package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import com.cx.utils.BeanHelper;
import com.cx.utils.Const;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

/**
 * Default implementation of the {@link org.springframework.data.repository.CrudRepository} interface. This will offer
 * you a more sophisticated interface than the plain {@link EntityManager} .
 * 
 * @author Alan Shu
 * @param <T> the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 */
@Transactional(readOnly=true, rollbackFor = Exception.class)
public class BaseJpaRedisRepositoryImpl<T extends RedisEntity<ID>, ID extends Serializable>
        extends SimpleJpaRepository<T, ID> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";

    private final RedisTemplate<String, ?> redisTemplate;

	private final RedisService redisService;

    private final BeanUtilsHashMapper<T> beanUtilsHashMapper;

    private final EntityManager em;

    private final Class<T> clazz;

    public BaseJpaRedisRepositoryImpl(Class<T> domainClass, EntityManager entityManager, RedisTemplate<String, ?> redisTemplate, RedisService redisService) {
        super(domainClass, entityManager);
        this.clazz = domainClass;
        this.em = entityManager;
        this.redisTemplate = redisTemplate;
        this.redisService = redisService;
        beanUtilsHashMapper = new BeanUtilsHashMapper(domainClass);
    }

    @Transactional
	public void delete(ID id) {
		Assert.notNull(id, ID_MUST_NOT_BE_NULL);

		try {
			super.delete(id);

			redisTemplate.delete(key(id));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void delete(T entity) {
		Assert.notNull(entity, "The entity must not be null!");

		try {
			super.delete(entity);

			redisTemplate.delete(key(entity.getId()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void delete(Iterable<? extends T> entities) {
		Assert.notNull(entities, "The given Iterable of entities not be null!");

		Set<String> keys = new HashSet<>();
		StringBuffer ids = new StringBuffer();
		try {
			entities.iterator().forEachRemaining(entity -> {
				ID id = entity.getId();
				keys.add(key(id));
				ids.append(id);
			});

			super.delete(entities);

			if(keys.size() == 0){
				return;
			}
			redisTemplate.delete(keys);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void deleteInBatch(Iterable<T> entities) {
		Assert.notNull(entities, "The given Iterable of entities not be null!");

		Set<String> keys = new HashSet<>();
		StringBuffer ids = new StringBuffer();
		try {
			entities.iterator().forEachRemaining(entity -> {
				ID id = entity.getId();
				keys.add(key(id));
				ids.append(id);
			});

			super.deleteInBatch(entities);

			if(keys.size() == 0){
				return;
			}
			redisTemplate.delete(keys);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void deleteAll() {
		try {
			super.deleteAll();

			Set<String> keys = entityKeys();
			if(keys.size() == 0){
				return;
			}
			redisTemplate.delete(keys);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void deleteAllInBatch() {
		try {
			super.deleteAllInBatch();

			Set<String> keys = entityKeys();
			if(keys.size() == 0){
				return;
			}
			redisTemplate.delete(keys);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public T findOne(ID id) {
		Assert.notNull(id, ID_MUST_NOT_BE_NULL);

		try {
			T t = getOnlyOne(key(id));
			if(null != t) {
				return t;
			}

			t = super.findOne(id);

			BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
			BeanHelper.registerConvertUtils();
			Map<String, String> map = beanUtilsHashMapper.toHash(t);
			map.entrySet().stream().forEach(item -> {
				operations.put(item.getKey(), item.getValue());
			});

			return t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T getOne(ID id) {
		Assert.notNull(id, ID_MUST_NOT_BE_NULL);

		try {
			T t = getOnlyOne(key(id));
			if(null != t) {
				return t;
			}

			t = super.getOne(id);

			BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
			BeanHelper.registerConvertUtils();
			Map<String, String> map = beanUtilsHashMapper.toHash(t);
			map.entrySet().stream().forEach(item -> {
				operations.put(item.getKey(), item.getValue());
			});

			return t;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean exists(ID id) {
		Assert.notNull(id, ID_MUST_NOT_BE_NULL);

		try {
			boolean exists = redisTemplate.hasKey(key(id));
			return exists ? exists : super.exists(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<T> findAll() {
		String idskey = key("findAll", null, null);
		List<String> entitykeys = entityKeys(idskey);
		final List<T> finalEntities = Lists.newArrayListWithCapacity(20);
		try {
		    if(!CollectionUtils.isEmpty(entitykeys)) {
//            finalEntities = (List<T>)redisTemplate.opsForValue().multiGet(entitykeys);
                entitykeys.stream().forEach(key -> {
                    T entity = getOnlyOne(key);
                    if (Objects.nonNull(entity)) {
                        finalEntities.add(entity);
                    }
                });

                if (!CollectionUtils.isEmpty(finalEntities)) {
                    return finalEntities;
                }
            }

			final List<T> ts = super.findAll();

			if(CollectionUtils.isEmpty(ts)){
				return ts;
			}

			List<String> ids = Lists.newArrayListWithCapacity(20);
			ts.stream().forEach(t ->
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

			return ts;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<T> findAll(Iterable<ID> ids) {
        String idskey = key("findAll", new String[]{"ids"}, new Object[]{ids});
        List<String> entitykeys = entityKeys(idskey);
        final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
        List<String> idList = Lists.newArrayListWithCapacity(20);
		ids.forEach(id -> {
            idList.add(id+"");
			T entity = getOnlyOne(key(id));
			if(Objects.nonNull(entity)){
                finalEntities.add(entity);
			}
		});

		if(!CollectionUtils.isEmpty(finalEntities)){
			return finalEntities;
		}

		final List<T> ts = super.findAll(ids);

		if(CollectionUtils.isEmpty(ts)){
			return ts;
		}

		ts.stream().forEach(t ->
				{
					BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(t.getId()));
					BeanHelper.registerConvertUtils();
					Map<String, String> map = beanUtilsHashMapper.toHash(t);
					map.entrySet().stream().forEach(item -> {
						operations.put(item.getKey(), item.getValue());
					});
				}
		);

        redisService.delete(idskey);
        redisService.putListCache(idskey, idList);

		return ts;
	}

	public List<T> findAll(Sort sort) {
        return super.findAll(sort);
	}

	public Page<T> findAll(Pageable pageable) {
		return super.findAll(pageable);
	}

	public T findOne(Specification<T> spec) {
        return super.findOne(spec);
	}

	public List<T> findAll(Specification<T> spec) {
		return super.findAll(spec);
	}

	public Page<T> findAll(Specification<T> spec, Pageable pageable) {
		return super.findAll(spec, pageable);
	}

	public List<T> findAll(Specification<T> spec, Sort sort) {
		return super.findAll(spec, sort);
	}

	@Override
	public <S extends T> S findOne(Example<S> example) {
		return super.findOne(example);
	}

	@Override
	public <S extends T> long count(Example<S> example) {
		return super.count(example);
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
		return super.exists(example);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		return super.findAll(example);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		return super.findAll(example, sort);
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return super.findAll(example, pageable);
	}

	public long count() {
		int size = entityKeys().size();
		return size == 0 ? super.count() : size;
	}

	public long count(Specification<T> spec) {
		return super.count(spec);
	}

	@Transactional
	public <S extends T> S save(S entity) {

		try{
            super.save(entity);

			if(null == entity.getId()){
                super.delete(entity);
			}

			BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(entity.getId()));
			BeanHelper.registerConvertUtils();
			Map<String, String> map = beanUtilsHashMapper.toHash(entity);
			map.entrySet().stream().forEach(item -> {
				operations.put(item.getKey(), item.getValue());
			});

			redisTemplate.delete(redisTemplate.keys(keyspace() + ":finds:*"));
			return entity;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public <S extends T> S saveAndFlush(S entity) {
        return super.saveAndFlush(entity);
	}

	@Transactional
	public <S extends T> List<S> save(Iterable<S> entities) {
		return super.save(entities);
	}

	@Transactional
	public void flush() {
        super.flush();
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

	private Set<String> entityKeys() {
		return redisTemplate.keys(keyspace() + ":ids:*");
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
}