package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import com.cx.utils.BeanHelper;
import com.cx.utils.Const;
import com.cx.utils.ObjWrapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.BeanUtilsHashMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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

            String key = key(id);
            Boolean flag = redisTemplate.hasKey(key);
            if(BooleanUtils.isTrue(flag)) {
                redisTemplate.delete(key);
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public void delete(T entity) {
		Assert.notNull(entity, "The entity must not be null!");

		try {
			super.delete(entity);

            String key = key(entity.getId());
            Boolean flag = redisTemplate.hasKey(key);
            if(BooleanUtils.isTrue(flag)) {
                redisTemplate.delete(key);
            }
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

            if(null == t) {
                return t;
            }

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

            if(null == t) {
                return t;
            }

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
			Boolean exists = redisTemplate.hasKey(key(id));
			return (exists != null && exists) ? exists : super.exists(id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<T> findAll() {
		String idskey = key("findAll", null, null);
		List<String> entitykeys = entityKeys(idskey);
		final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
		try {
		    if(!CollectionUtils.isEmpty(entitykeys)) {
//            finalEntities = (List<T>)redisTemplate.opsForValue().multiGet(entitykeys);
                entitykeys.stream().forEach(key -> {
                    T entity = getOnlyOne(key(key));
                    if (Objects.nonNull(entity)) {
                        finalEntities.add(entity);
                    }
                });

                if (!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)) {
                    return finalEntities;
                }
            }

			final List<T> ts = super.findAll();

			if(CollectionUtils.isEmpty(ts)){
				return ts;
			}

			List<String> ids = Lists.newArrayListWithCapacity(10);
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
        List<String> idList = Lists.newArrayListWithCapacity(10);
        try {
            ids.forEach(id -> {
                idList.add(id+"");
                T entity = getOnlyOne(key(id));
                if(Objects.nonNull(entity)){
                    finalEntities.add(entity);
                }
            });

            if(!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)){
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	public List<T> findAll(Sort sort) {
        return findAll(super.findAll(sort), sort, null);
	}

	public Page<T> findAll(Pageable pageable) {
        return findAll(super.findAll(pageable), pageable, null);
	}

	public T findOne(Specification<T> spec) {
        int conditionsHashcode = spec.hashCode();
        String idskey = key("findOne", new String[]{"spec"}, new Object[]{conditionsHashcode});
        String entitykey = entityKey(idskey);
        try {
            if(StringUtils.isNotBlank(entitykey)) {
                T entity = getOnlyOne(entitykey);

                if (!ObjectUtils.isEmpty(entity)) {
                    return entity;
                }
            }

            final T t = super.findOne(spec);

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

	public List<T> findAll(Specification<T> spec) {
        return findAll(super.findAll(spec), null, spec);
	}

	public Page<T> findAll(Specification<T> spec, Pageable pageable) {
        return findAll(super.findAll(spec, pageable), pageable, spec);
	}

	public List<T> findAll(Specification<T> spec, Sort sort) {
        return findAll(super.findAll(spec, sort), sort, spec);
	}

	@Override
	public <S extends T> S findOne(Example<S> example) {
        int conditionsHashcode = example.hashCode();
        String idskey = key("findOne", new String[]{"spec"}, new Object[]{conditionsHashcode});
        String entitykey = entityKey(idskey);
        try {
            if(StringUtils.isNotBlank(entitykey)) {
                S entity = (S)getOnlyOne(entitykey);//父类就是子类，故强转

                if (!ObjectUtils.isEmpty(entity)) {
                    return entity;
                }
            }

            final S s = super.findOne(example);

            if(null == s) {
                return s;
            }

            BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(s.getId()));
            BeanHelper.registerConvertUtils();
            Map<String, String> map = beanUtilsHashMapper.toHash(s);
            map.entrySet().stream().forEach(item -> {
                operations.put(item.getKey(), item.getValue());
            });

            redisService.delete(idskey);
            redisService.putObjCache(idskey, s.getId());

            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public <S extends T> long count(Example<S> example) {
        int conditionsHashcode = example.hashCode();
        String idskey = key("count", new String[]{"count"}, new Object[]{conditionsHashcode});
        ObjWrapper<Long> objWrapper = (ObjWrapper<Long>)countKey(idskey);
        try {
            if(ObjectUtils.isEmpty(objWrapper)) {
                return objWrapper.getData();
            }

            final long l = super.count(example);

            redisService.delete(idskey);
            redisService.putObjCache(idskey, new ObjWrapper<Long>(l));

            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
        int conditionsHashcode = example.hashCode();
        String idskey = key("exists", new String[]{"exists"}, new Object[]{conditionsHashcode});
        ObjWrapper<Boolean> objWrapper = (ObjWrapper<Boolean>)countKey(idskey);
        try {
            if(ObjectUtils.isEmpty(objWrapper)) {
                return objWrapper.getData();
            }

            final boolean l = super.exists(example);

            redisService.delete(idskey);
            redisService.putObjCache(idskey, new ObjWrapper<Boolean>(l));

            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
        return findAll(super.findAll(example), example, null);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return findAll(super.findAll(example, sort), example, sort);
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return findAll(super.findAll(example, pageable), example, pageable);
	}

	public long count() {
        int size = -1;
        int eks = entityKeys().size();
        if(eks > -1){
            size = eks;
        }
		return size == -1 ? super.count() : size;
	}

	public long count(Specification<T> spec) {
        int conditionsHashcode = spec.hashCode();
        String idskey = key("count", new String[]{"count"}, new Object[]{conditionsHashcode});
        ObjWrapper<Long> objWrapper = (ObjWrapper<Long>)countKey(idskey);
        try {
            if(ObjectUtils.isEmpty(objWrapper)) {
                return objWrapper.getData();
            }

            final long l = super.count(spec);

            redisService.delete(idskey);
            redisService.putObjCache(idskey, new ObjWrapper<Long>(l));

            return l;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	@Transactional
	public <S extends T> S save(S entity) {
		try{
            String key = key(entity.getId());
            Boolean flag = redisTemplate.hasKey(key);
            if(BooleanUtils.isTrue(flag)) {
                redisTemplate.delete(key);
            }

            super.save(entity);

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
        try{
            String key = key(entity.getId());
            Boolean flag = redisTemplate.hasKey(key);
            if(BooleanUtils.isTrue(flag)) {
                redisTemplate.delete(key);
            }

            super.saveAndFlush(entity);

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
	public <S extends T> List<S> save(Iterable<S> entities) {
        try{
            entities.forEach(entity -> {
                String key = key(entity.getId());
                Boolean flag = redisTemplate.hasKey(key);
                if(BooleanUtils.isTrue(flag)) {
                    redisTemplate.delete(key);
                }
            });

            super.save(entities);

            entities.forEach(entity -> {
                BoundHashOperations<String, String, String> operations = redisTemplate.boundHashOps(key(entity.getId()));
                BeanHelper.registerConvertUtils();
                Map<String, String> map = beanUtilsHashMapper.toHash(entity);
                map.entrySet().stream().forEach(item -> {
                    operations.put(item.getKey(), item.getValue());
                });
            });

            redisTemplate.delete(redisTemplate.keys(keyspace() + ":finds:*"));

            return (List<S>)entities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

	private String key(String id){
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

	private Set<String> entityKeys() {
		return redisTemplate.keys(keyspace() + ":ids:*");
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

	private List<T> findAll(List<T> result, Sort sort, Specification<T> spec) {
		String[] paramnames = new String[2];
		Object[] paramvals = new Object[2];
		if(com.cx.utils.ObjectUtils.anyNotNull(spec, sort)){
			if(!ObjectUtils.isEmpty(spec)){
				paramnames[0] = "spec";
				paramvals[0] = spec.hashCode();
			}
			if(!ObjectUtils.isEmpty(sort)){
				paramnames[1] = "sort";
				paramvals[1] = sort.hashCode();
			}
		}

		String idskey = key("findAll", paramnames, paramvals);
		List<String> entitykeys = entityKeys(idskey);
		final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
		try {
			if(!CollectionUtils.isEmpty(entitykeys)) {
//            finalEntities = (List<T>)redisTemplate.opsForValue().multiGet(entitykeys);
				entitykeys.stream().forEach(key -> {
					T entity = getOnlyOne(key);
					if (Objects.nonNull(entity)) {
						finalEntities.add(entity);
					}
				});

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

    private <S extends T> List<S> findAll(List<S> result, Example<S> example, Sort sort) {
        String[] paramnames = new String[2];
        Object[] paramvals = new Object[2];
        if(com.cx.utils.ObjectUtils.anyNotNull(example, sort)){
            if(!ObjectUtils.isEmpty(example)){
                paramnames[0] = "example";
                paramvals[0] = example.hashCode();
            }
            if(!ObjectUtils.isEmpty(sort)){
                paramnames[1] = "sort";
                paramvals[1] = sort.hashCode();
            }
        }

        String idskey = key("findAll", paramnames, paramvals);
        List<String> entitykeys = entityKeys(idskey);
        final List<S> finalEntities = Lists.newArrayListWithCapacity(10);
        try {
            if(!CollectionUtils.isEmpty(entitykeys)) {
//            finalEntities = (List<S>)redisTemplate.opsForValue().multiGet(entitykeys);
                entitykeys.stream().forEach(key -> {
                    S entity = (S)getOnlyOne(key);//父类就是子类，故强转
                    if (Objects.nonNull(entity)) {
                        finalEntities.add(entity);
                    }
                });

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

    private Page<T> findAll(Page<T> result, Pageable pageable, Specification<T> spec) {
        String[] paramnames = new String[2];
        Object[] paramvals = new Object[2];
        if(com.cx.utils.ObjectUtils.anyNotNull(spec, pageable)){
            if(!ObjectUtils.isEmpty(spec)){
                paramnames[0] = "spec";
                paramvals[0] = spec.hashCode();
            }
            if(!ObjectUtils.isEmpty(pageable)){
                paramnames[1] = "pageable";
                paramvals[1] = pageable.hashCode();
            }
        }

        String idskey = key("findAll", paramnames, paramvals);
        List<String> entitykeys = entityKeys(idskey);
        final List<T> finalEntities = Lists.newArrayListWithCapacity(10);
        try {
            if(!CollectionUtils.isEmpty(entitykeys)) {
                entitykeys.stream().forEach(key -> {
                    T entity = getOnlyOne(key);
                    if (Objects.nonNull(entity)) {
                        finalEntities.add(entity);
                    }
                });

                if (!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)) {
                    return new PageImpl(finalEntities);
                }
            }

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

    private <S extends T> Page<S> findAll(Page<S> result, Example<S> example, Pageable pageable) {
        String[] paramnames = new String[2];
        Object[] paramvals = new Object[2];
        if(com.cx.utils.ObjectUtils.anyNotNull(example, pageable)){
            if(!ObjectUtils.isEmpty(example)){
                paramnames[0] = "example";
                paramvals[0] = example.hashCode();
            }
            if(!ObjectUtils.isEmpty(pageable)){
                paramnames[1] = "pageable";
                paramvals[1] = pageable.hashCode();
            }
        }

        String idskey = key("findAll", paramnames, paramvals);
        List<String> entitykeys = entityKeys(idskey);
        final List<S> finalEntities = Lists.newArrayListWithCapacity(10);
        try {
            if(!CollectionUtils.isEmpty(entitykeys)) {
                entitykeys.stream().forEach(key -> {
                    S entity = (S)getOnlyOne(key);//父类就是子类，故强转
                    if (Objects.nonNull(entity)) {
                        finalEntities.add(entity);
                    }
                });

                if (!CollectionUtils.isEmpty(finalEntities) && !CollectionUtils.isEmpty(entitykeys)) {
                    return new PageImpl(finalEntities);
                }
            }

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
}