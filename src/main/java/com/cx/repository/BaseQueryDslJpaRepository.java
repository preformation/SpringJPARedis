package com.cx.repository;

import com.cx.utils.BeanHelper;
import com.cx.utils.Const;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import org.springframework.data.domain.Page;
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

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
public class BaseQueryDslJpaRepository<T, ID extends Serializable> extends QueryDslJpaRepository<T, ID> {

	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

	private final EntityPath<T> path;
	private final PathBuilder<T> builder;
	private final Querydsl querydsl;

    private final RedisTemplate<String, ?> redisTemplate;

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
	public BaseQueryDslJpaRepository(JpaEntityInformation<T, ID> entityInformation, Class<T> domainClass, EntityManager entityManager, RedisTemplate<String, ?> redisTemplate) {
		super(entityInformation, entityManager);
		this.path = DEFAULT_ENTITY_PATH_RESOLVER.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
		this.querydsl = new Querydsl(entityManager, builder);

        this.clazz = domainClass;
        this.em = entityManager;
        this.redisTemplate = redisTemplate;
        beanUtilsHashMapper = new BeanUtilsHashMapper(domainClass);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findOne(com.mysema.query.types.Predicate)
	 */
	@Override
	public T findOne(Predicate predicate) {
		return createQuery(predicate).select(path).fetchOne();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate)
	 */
	@Override
	public List<T> findAll(Predicate predicate) {
		return createQuery(predicate).select(path).fetch();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, com.mysema.query.types.OrderSpecifier<?>[])
	 */
	@Override
	public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
		return executeSorted(createQuery(predicate).select(path), orders);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Sort)
	 */
	@Override
	public List<T> findAll(Predicate predicate, Sort sort) {
		return executeSorted(createQuery(predicate).select(path), sort);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.OrderSpecifier[])
	 */
	@Override
	public List<T> findAll(OrderSpecifier<?>... orders) {
		return executeSorted(createQuery(new Predicate[0]).select(path), orders);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<T> findAll(Predicate predicate, Pageable pageable) {

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
		return createQuery(predicate).fetchCount();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#exists(com.mysema.query.types.Predicate)
	 */
	@Override
	public boolean exists(Predicate predicate) {
		return createQuery(predicate).fetchCount() > 0;
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

    private Set<String> entityKeys() {
        return redisTemplate.keys(keyspace() + ":ids:*");
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