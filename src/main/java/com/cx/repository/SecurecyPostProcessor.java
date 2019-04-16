package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import com.cx.utils.BeanHelper;
import com.cx.utils.ListWrapper;
import com.google.common.collect.Lists;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.cx.utils.Const.REDIS_2ND_KEY_PRE;

public class SecurecyPostProcessor<T extends RedisEntity<ID>, ID extends Serializable> implements RepositoryProxyPostProcessor {

    @Autowired
    private RedisTemplate<String, ?> redisTemplate;
    @Autowired
    private RedisService redisService;

    @Override
	public void postProcess(ProxyFactory factory, RepositoryInformation information) {
		factory.addAdvice(new SecurecyAdvice<T, ID>(redisTemplate, redisService, (Class<T>)information.getDomainType()));
	}

    private class SecurecyAdvice<T extends RedisEntity<ID>, ID extends Serializable> implements MethodInterceptor {

        private final RedisTemplate<String, ?> redisTemplate;

        private final RedisService redisService;

        private final BeanUtilsHashMapper<T> beanUtilsHashMapper;

        private final Class<T> domainClass;

        public SecurecyAdvice(RedisTemplate<String, ?> redisTemplate, RedisService redisService, Class<T> domainClass) {
            this.redisTemplate = redisTemplate;
            this.redisService = redisService;
            this.domainClass = domainClass;
            beanUtilsHashMapper = new BeanUtilsHashMapper(domainClass);
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {

            String methodName = invocation.getMethod().getName();
            int lenth = methodName.length();
            boolean isAnnotationPresent = invocation.getMethod().isAnnotationPresent(Query.class);
            if(methodName.startsWith("findBy") || isAnnotationPresent) {
                String idskey = key("findBy", new String[]{methodName.substring(6, lenth)}, invocation.getArguments());
                Type returnType = invocation.getMethod().getGenericReturnType();

                if(returnType.getTypeName().startsWith("java.util.List")){
                    ParameterizedType paramType = (ParameterizedType)returnType;
                    Type[] types = paramType.getActualTypeArguments();
                    if(ArrayUtils.isNotEmpty(types)){
                        if(types[0].getTypeName().equals(domainClass.getTypeName())){
                            List<T> list = findByToRedis(idskey);
                            if (!CollectionUtils.isEmpty(list)) {
                                return list;
                            }
                        }else{
                            ListWrapper listWrapper = redisService.getObjCache(idskey, ListWrapper.class);//(Class<?>) types[0]
                            if (!ObjectUtils.isEmpty(listWrapper) && !CollectionUtils.isEmpty(listWrapper.getData())) {
                                return listWrapper.getData();
                            }
                        }
                    }else {
                        ListWrapper lw = redisService.getObjCache(idskey, ListWrapper.class);
                        if (!ObjectUtils.isEmpty(lw) && !CollectionUtils.isEmpty(lw.getData())) {
                            return lw.getData();
                        }
                    }
                } else {
                    if(returnType.getTypeName().equals(domainClass.getTypeName())){
                        List<T> list = findByToRedis(idskey);
                        if (!CollectionUtils.isEmpty(list)) {
                            return list.get(0);
                        }
                    }else{
                        ListWrapper lws = redisService.getObjCache(idskey, ListWrapper.class);
                        if (!ObjectUtils.isEmpty(lws) && !CollectionUtils.isEmpty(lws.getData())) {
                            return lws.getData().get(0);
                        }
                    }
                }

                Object obj = invocation.proceed();
                if(ObjectUtils.isEmpty(obj)){
                    return obj;
                }
                if(returnType.getTypeName().startsWith("java.util.List")){
                    ParameterizedType paramType = (ParameterizedType)returnType;
                    Type[] types = paramType.getActualTypeArguments();
                    if(ArrayUtils.isNotEmpty(types)){
                        if(types[0].getTypeName().equals(domainClass.getTypeName())){
                            List<T> list = (List<T>)obj;
                            saveFindByToRedis(list, idskey);
                            return obj;
                        }else{
                            List<?> list = (List<?>)obj;
                            redisService.putObjCache(idskey, new ListWrapper(list));
                            return obj;
                        }
                    }else {
                        List<?> list = (List<?>)obj;
                        redisService.putObjCache(idskey, new ListWrapper(list));
                        return obj;
                    }
                } else {
                    if(returnType.getTypeName().equals(domainClass.getTypeName())){
                        List<T> list = Lists.newArrayListWithCapacity(1);
                        T t = (T)obj;
                        if(!ObjectUtils.isEmpty(t.getId())) {//防止空对象
                            list.add(t);
                        }
                        saveFindByToRedis(list, idskey);
                        return obj;
                    }else{
                        List<Object> list = Lists.newArrayListWithCapacity(1);
                        list.add(obj);
                        redisService.putObjCache(idskey, new ListWrapper(list));
                        return obj;
                    }
                }
            }

            return invocation.proceed();
        }

        /**
         *  特殊查询，先缓存key，调用该查询缓存是哪些ID时，先取出对应缓存key的全部ID值，再去缓存找ID KEY类型的所有缓存值
         * @param idskey
         * @return
         */
        public List<T> findByToRedis(String idskey) {
            final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
            try {
                List<String> entitykeys = entityKeys(idskey);
                if(!CollectionUtils.isEmpty(entitykeys)) {
                    entitykeys.stream().forEach(key -> {
                        T entity = getOnlyOne(keyspace() + ":ids:" + key);
                        if(Objects.nonNull(entity)){
                            finalEntities.add(entity);
                        }
                    });
                }

                return finalEntities;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         *  保存返回结果为List<T> findby到redis
         * @return
         */
        public List<T> saveFindByToRedis(List<T> entities, String idskey) {
            if (StringUtils.isBlank(idskey) || CollectionUtils.isEmpty(entities)) {
                return entities;
            }

            List<String> ids = Lists.newArrayListWithCapacity(10);
            try {
                entities.stream().forEach(t ->
                        {
                            ids.add(t.getId()+"");
                            BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
                            BeanHelper.registerConvertUtils();
                            Map<String, String> map = beanUtilsHashMapper.toHash(t);
                            operations.putAll(map);
                        }
                );

                redisService.delete(idskey);
                redisService.putListCache(idskey, ids);
//                BoundListOperations operations = redisTemplate.boundListOps(idskey);
//                operations.getOperations().delete(idskey);
//                operations.rightPushAll(ids.toArray());

                return (List<T>) entities;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String keyspace(){
            return REDIS_2ND_KEY_PRE + domainClass.getSimpleName();
        }

        public String key(ID id){
            return keyspace() + ":ids:" + id;
        }

        /**
         * findBy缓存key
         * @param methodname
         * @param paramnames 第一个，第二个，...依次排列
         * @param paramvals 第一个，第二个，...依次排列，和paramnames的顺序一致
         * @return
         */
        public String key(String methodname, String[] paramnames, Object[] paramvals){
            StringBuffer sb = new StringBuffer(keyspace());
            sb.append(":finds").append(StringUtils.isBlank(methodname)? "" : ":"+methodname);
            if(ArrayUtils.isNotEmpty(paramnames) && ArrayUtils.isNotEmpty(paramvals)) {
                Arrays.stream(paramnames).filter(p->null!=p).forEach(fieldname -> {
                    sb.append(":"+fieldname);
                });

                Arrays.stream(paramvals).filter(p->null!=p).forEach(paramval -> {
                    if (paramval instanceof List) {
                        Collections.sort((List) paramval);
                    }
                    if (paramval instanceof Object[]) {
                        Arrays.sort((Object[]) paramval);
                    }
                    sb.append(":" + paramval.hashCode());
                });
            }

            return sb.toString();
        }

        private List<String> entityKeys(String key){
            Boolean hasKey = redisTemplate.hasKey(key);
            if(!hasKey){
                return null;
            }

            return redisService.getListCache(key, String.class);
//            BoundListOperations<String, ?> operations = redisTemplate.boundListOps(key);
//            return (List<String>)operations.range(0, -1);
        }

        private T getOnlyOne(String key){
            Boolean hasKey = redisTemplate.hasKey(key);
            if(!hasKey){
                return null;
            }
            BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key);
            Map<String, String> entries = operations.entries();
            BeanHelper.registerConvertUtils();
            return beanUtilsHashMapper.fromHash(entries);
        }
    }
}