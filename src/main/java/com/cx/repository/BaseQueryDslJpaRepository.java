package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import com.cx.utils.BeanHelper;
import com.cx.utils.Const;
import com.cx.utils.ObjWrapper;
import com.google.common.collect.Lists;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.*;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QSort;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.repository.support.PageableExecutionUtils.TotalSupplier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * QueryDsl specific extension of {@link SimpleJpaRepository} which adds implementation for
 * {@link QueryDslPredicateExecutor}.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 * @author Jocelyn Ntakpe
 * @author Christoph Strobl
 */
public class BaseQueryDslJpaRepository<T extends RedisEntity<ID>, ID extends Serializable> extends QueryDslJpaRepository<T, ID> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

	private final EntityPath<T> path;
	private final PathBuilder<T> builder;
	private final Querydsl querydsl;

    private final RedisTemplate<String, ?> redisTemplate;

	private final RedisService redisService;

    private final BeanUtilsHashMapper<T> beanUtilsHashMapper;

    private final EntityManager em;

    private final Class<T> clazz;

	/**
	 * Creates a new {@link BaseQueryDslJpaRepository} from the given domain class and {@link EntityManager} and uses the
	 * given {@link EntityPathResolver} to translate the domain class into an {@link EntityPath}.
	 * 
	 * @param entityInformation must not be {@literal null}.
	 * @param entityManager must not be {@literal null}.
	 */
	public BaseQueryDslJpaRepository(JpaEntityInformation<T, ID> entityInformation, Class<T> domainClass, EntityManager entityManager, RedisTemplate<String, ?> redisTemplate, RedisService redisService) {
		super(entityInformation, entityManager);
		this.path = DEFAULT_ENTITY_PATH_RESOLVER.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
		this.querydsl = new Querydsl(entityManager, builder);

        this.clazz = domainClass;
        this.em = entityManager;
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
        beanUtilsHashMapper = new BeanUtilsHashMapper(domainClass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findOne(com.mysema.query.types.Predicate)
	 */
	@Override
	public T findOne(Predicate predicate) {
        int conditionsHashcode = predicate.hashCode();
        String idskey = key("findOne", new String[]{"predicate"}, new Object[]{conditionsHashcode});
        String entitykey = entityKey(idskey);
        try {
            if(StringUtils.isNotBlank(entitykey)) {
                T entity = getOnlyOne(entitykey);

                if (!ObjectUtils.isEmpty(entity)) {
                    return entity;
                }
            }

            final T t = createQuery(predicate).select(path).fetchOne();

            if(null == t) {
                return t;
            }

            BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
            BeanHelper.registerConvertUtils();
            Map<String, String> map = beanUtilsHashMapper.toHash(t);
            map.entrySet().stream().forEach(item -> {
                operations.put(item.getKey(), item.getValue());
            });

            redisService.delete(idskey);
            redisService.putObjCache(idskey, t.getId());

            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate)
	 */
	@Override
	public List<T> findAll(Predicate predicate) {
        return findAll(createQuery(predicate).select(path).fetch(), predicate, null, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, com.mysema.query.types.OrderSpecifier<?>[])
	 */
	@Override
	public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        return findAll(executeSorted(createQuery(predicate).select(path), orders), predicate, null, orders);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Predicate predicate, Sort sort) {
        return findAll(executeSorted(createQuery(predicate).select(path), sort), predicate, sort, null);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.OrderSpecifier[])
	 */
	@Override
	public List<T> findAll(OrderSpecifier<?>... orders) {
        return findAll(executeSorted(createQuery(new Predicate[0]).select(path), orders), null, null, orders);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {
        String[] paramnames = new String[2];
        Object[] paramvals = new Object[2];
        if(com.cx.utils.ObjectUtils.anyNotNull(predicate, pageable)){
            if(!ObjectUtils.isEmpty(predicate)){
                paramnames[0] = "predicate";
                paramvals[0] = predicate.hashCode();
            }
            if(!ObjectUtils.isEmpty(pageable)){
                paramnames[1] = "pageable";
                paramvals[1] = pageable.hashCode();
            }
        }

        String idskey = key("findAll", paramnames, paramvals);
        List<String> entitykeys = entityKeys(idskey);
        final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
        List<String> idkeyList = Lists.newArrayListWithCapacity(10);
        try {
            if(!CollectionUtils.isEmpty(entitykeys)) {
                entitykeys.stream().forEach(key -> {
                    idkeyList.add(key);
//                    T entity = getOnlyOne(key);
//                    if (Objects.nonNull(entity)) {
//                        finalEntities.add(entity);
//                    }
                });
                finalEntities.addAll((List<T>)redisTemplate.opsForValue().multiGet(idkeyList));

                if (!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)) {
                    return new PageImpl(finalEntities);
                }
            }

            Page<T> result = findAllJpa(predicate, pageable);

            if(CollectionUtils.isEmpty(result.getContent())){
                return result;
            }

            List<String> ids = Lists.newArrayListWithCapacity(10);
            result.getContent().stream().forEach(t ->
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

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

    private Page<T> findAllJpa(Predicate predicate, Pageable pageable) {

        final JPQLQuery<?> countQuery = createCountQuery(predicate);
        JPQLQuery<T> query = querydsl.applyPagination(pageable, createQuery(predicate).select(path));

        return PageableExecutionUtils.getPage(query.fetch(), pageable, new TotalSupplier() {

            @Override
            public long get() {
                return countQuery.fetchCount();
            }
        });
    }

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#count(com.mysema.query.types.Predicate)
	 */
	@Override
	public long count(Predicate predicate) {
        int conditionsHashcode = predicate.hashCode();
        String idskey = key("count", new String[]{"count"}, new Object[]{conditionsHashcode});
        ObjWrapper<Long> objWrapper = (ObjWrapper<Long>)countKey(idskey);
        try {
            if(ObjectUtils.isEmpty(objWrapper)) {
                return objWrapper.getData();
            }

            final long l = createQuery(predicate).fetchCount();

            redisService.delete(idskey);
            redisService.putObjCache(idskey, new ObjWrapper<Long>(l));

            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#exists(com.mysema.query.types.Predicate)
	 */
	@Override
	public boolean exists(Predicate predicate) {
		return this.count(predicate) > 0;
	}

	/**
	 * Creates a new {@link JPQLQuery} for the given {@link Predicate}.
	 * 
	 * @param predicate
	 * @return the Querydsl {@link JPQLQuery}.
	 */
	protected JPQLQuery<?> createQuery(Predicate... predicate) {
        AbstractJPAQuery<?, ?> query = querydsl.createQuery(path).where(predicate);
        CrudMethodMetadata metadata = getRepositoryMethodMetadata();

        if (metadata == null) {
            return query;
        }

        LockModeType type = metadata.getLockModeType();
        query = type == null ? query : query.setLockMode(type);

        for (Entry<String, Object> hint : getQueryHints().entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }

        return query;
	}

	/**
	 * Creates a new {@link JPQLQuery} count query for the given {@link Predicate}.
	 *
	 * @param predicate, can be {@literal null}.
	 * @return the Querydsl count {@link JPQLQuery}.
	 */
	protected JPQLQuery<?> createCountQuery(Predicate predicate) {
        AbstractJPAQuery<?, ?> query = querydsl.createQuery(path).where(predicate);

        CrudMethodMetadata metadata = getRepositoryMethodMetadata();

        if (metadata == null) {
            return query;
        }

        for (Entry<String, Object> hint : metadata.getQueryHints().entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }

        return query;
	}

	/**
	 * Executes the given {@link JPQLQuery} after applying the given {@link OrderSpecifier}s.
	 * 
	 * @param query must not be {@literal null}.
	 * @param orders must not be {@literal null}.
	 * @return
	 */
	private List<T> executeSorted(JPQLQuery<T> query, OrderSpecifier<?>... orders) {
		return executeSorted(query, new QSort(orders));
	}

	/**
	 * Executes the given {@link JPQLQuery} after applying the given {@link Sort}.
	 * 
	 * @param query must not be {@literal null}.
	 * @param sort must not be {@literal null}.
	 * @return
	 */
	private List<T> executeSorted(JPQLQuery<T> query, Sort sort) {
		return querydsl.applySorting(sort, query).fetch();
	}

    private String keyspace(){
        return Const.REDIS_2ND_KEY_PRE + getDomainClass().getSimpleName();
    }

    private String key(ID id){
        return keyspace() + ":ids:" + id;
    }

    /**
     * findAll缓存key
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

    private ObjWrapper countKey(String key){
        Boolean hasKey = redisTemplate.hasKey(key);
        if(!hasKey){
            return null;
        }

        return redisService.getObjCache(key, ObjWrapper.class);
    }

    private String entityKey(String key){
        Boolean hasKey = redisTemplate.hasKey(key);
        if(!hasKey){
            return null;
        }

        return redisService.getObjCache(key, String.class);
    }

	private List<String> entityKeys(String key){
		Boolean hasKey = redisTemplate.hasKey(key);
		if(!hasKey){
			return null;
		}

		return redisService.getListCache(key, String.class);
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


    private List<T> findAll(List<T> result, Predicate predicate, Sort sort, OrderSpecifier<?>... orders) {
        String[] paramnames = new String[3];
        Object[] paramvals = new Object[3];
        if(com.cx.utils.ObjectUtils.anyNotNull(predicate, sort, orders)){
            if(!ObjectUtils.isEmpty(predicate)){
                paramnames[0] = "predicate";
                paramvals[0] = predicate.hashCode();
            }
            if(!ObjectUtils.isEmpty(sort)){
                paramnames[1] = "sort";
                paramvals[1] = sort.hashCode();
            }
            if(!ObjectUtils.isEmpty(orders)){
                paramnames[2] = "orders";
                paramvals[2] = orders.hashCode();
            }
        }

        String idskey = key("findAll", paramnames, paramvals);
        List<String> entitykeys = entityKeys(idskey);
        final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
        List<String> idkeyList = Lists.newArrayListWithCapacity(10);
        try {
            if(!CollectionUtils.isEmpty(entitykeys)) {
                entitykeys.stream().forEach(key -> {
                    idkeyList.add(key);
//                    T entity = getOnlyOne(key);
//                    if (Objects.nonNull(entity)) {
//                        finalEntities.add(entity);
//                    }
                });
                finalEntities.addAll((List<T>)redisTemplate.opsForValue().multiGet(idkeyList));

                if (!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)) {
                    return finalEntities;
                }
            }

            if(CollectionUtils.isEmpty(result)){
                return result;
            }

            List<String> ids = Lists.newArrayListWithCapacity(10);
            result.stream().forEach(t ->
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

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}