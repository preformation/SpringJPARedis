package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import com.cx.utils.BeanHelper;
import com.google.common.collect.Lists;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.cx.utils.Const.REDIS_2ND_KEY_PRE;

@Component
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
            if(methodName.startsWith("findBy")) {
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
                            List<?> list = redisService.getListCache(idskey, (Class<? extends Object>) types[0]);
                            if (!CollectionUtils.isEmpty(list)) {
                                return list;
                            }
                        }
                    }else {
                        List<Object> list = redisService.getListCache(idskey, Object.class);
                        if (!CollectionUtils.isEmpty(list)) {
                            return list;
                        }
                    }
                } else {
                    if(returnType.getTypeName().equals(domainClass.getTypeName())){
                        List<T> list = findByToRedis(idskey);
                        if (!CollectionUtils.isEmpty(list)) {
                            return list.get(0);
                        }
                    }else{
                        List<?> list = redisService.getListCache(idskey, returnType.getClass());
                        if (!CollectionUtils.isEmpty(list)) {
                            return list.get(0);
                        }
                    }
                }

                Object obj = invocation.proceed();
                if(returnType.getTypeName().startsWith("java.util.List")){
                    ParameterizedType paramType = (ParameterizedType)returnType;
                    Type[] types = paramType.getActualTypeArguments();
                    if(ArrayUtils.isNotEmpty(types)){
                        if(types[0].getTypeName().equals(domainClass.getTypeName())){
                            List<T> list = (List<T>)obj;
                            return CollectionUtils.isEmpty(list)?list:saveFindByToRedis(list, idskey);
                        }else{
                            List<?> list = (List<?>)obj;
                            return CollectionUtils.isEmpty(list)?Lists.newArrayListWithCapacity(0):redisService.putListCache(idskey, list);
                        }
                    }else {
                        List<Object> list = (List<Object>)obj;
                        return CollectionUtils.isEmpty(list)?Lists.newArrayListWithCapacity(0):redisService.putListCache(idskey, list);
                    }
                } else {
                    if(returnType.getTypeName().equals(domainClass.getTypeName())){
                        List<T> list = Lists.newArrayListWithCapacity(1);
                        list.add((T)obj);
                        return CollectionUtils.isEmpty(list)?null:saveFindByToRedis(list, idskey).get(0);
                    }else{
                        List<Object> list = Lists.newArrayListWithCapacity(1);
                        list.add(obj);
                        redisService.putListCache(idskey, list);
                        return CollectionUtils.isEmpty(list)?null:obj;
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
            List<String> entitykeys = entityKeys(idskey);
            final List<T> finalEntities = Lists.newArrayListWithCapacity(20);

            try {
                if(null != entitykeys && !entitykeys.isEmpty()) {
//                finalEntities = (List<T>) redisTemplate.opsForValue().multiGet(entitykeys);
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
            if (StringUtils.isAnyBlank(idskey) || CollectionUtils.isEmpty(entities)) {
                throw new RuntimeException("保存findBys方法部分传参不能为空");
            }

            try {
                List<String> ids = Lists.newArrayListWithCapacity(20);
                entities.stream().forEach(t ->
                        {
                            ids.add(t.getId()+"");
                            BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
                            BeanHelper.registerConvertUtils();
                            Map<String, String> map = beanUtilsHashMapper.toHash(t);
                            map.entrySet().stream().forEach(item -> {
                                operations.put(item.getKey(), item.getValue());
                            });
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
                Arrays.stream(paramnames).forEach(fieldname -> {
                    sb.append(":"+fieldname);
                });

                Arrays.stream(paramvals).forEach(paramval -> {
                    if(paramval instanceof List){
                        Collections.sort((List)paramval);
                    }
                    if(paramval instanceof Object[]) {
                        Arrays.sort((Object[])paramval);
                    }
                    sb.append(":"+paramval.hashCode());
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