package com.cx.repository;

import com.cx.entity.RedisEntity;
import com.cx.service.impl.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

import static org.springframework.data.querydsl.QueryDslUtils.QUERY_DSL_PRESENT;

/**
 *
 * @author Alan Shu
 **/
@SuppressWarnings({"rawtypes","unchecked"})
public class BaseRepositoryFactoryBean<R extends Repository<T, ID>, T extends RedisEntity<ID>, ID extends Serializable> extends JpaRepositoryFactoryBean<R, T, ID> {

    @Autowired
    private RedisTemplate<String, ?> redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SecurecyPostProcessor<T, ID> securecyPostProcessor;

    private final Class<? extends R> repositoryInterface;

    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public BaseRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory(EntityManager em) {
        BaseRepositoryFactory baseRepositoryFactory = new BaseRepositoryFactory<T, ID>(em, redisTemplate, redisService, repositoryInterface);
        baseRepositoryFactory.addRepositoryProxyPostProcessor(securecyPostProcessor);
        return baseRepositoryFactory;
    }

    /**
     * 创建一个内部类，该类不用在外部访问
     * @param <T>
     * @param <ID>
     */
    private class BaseRepositoryFactory<T extends RedisEntity<ID>, ID extends Serializable> extends JpaRepositoryFactory {

        private final EntityManager em;
        private final RedisTemplate<String, ?> redisTemplate;
        private final RedisService redisService;
        private final Class<? extends R> repositoryInterface;

        public BaseRepositoryFactory(EntityManager em, RedisTemplate<String, ?> redisTemplate, RedisService redisService, Class<? extends R> repositoryInterface) {
            super(em);
            this.em = em;
            this.redisTemplate = redisTemplate;
            this.redisService = redisService;
            this.repositoryInterface = repositoryInterface;
        }

        /**
         * 设置具体的实现类是BaseJpaRedisRepository
         * @param information
         * @return
         */
		@Override
        protected Object getTargetRepository(RepositoryInformation information) {
            Class<T> domainClass = (Class<T>) information.getDomainType();
            final SimpleJpaRepository<T, ID> repository;
            if (isQueryDslExecutor(repositoryInterface)) {
                repository = new BaseQueryDslJpaRepository(JpaEntityInformationSupport.getEntityInformation(domainClass, em), domainClass, em, redisTemplate, redisService);
            } else {
                repository = new BaseJpaRedisRepositoryImpl<T, ID>(domainClass, em, redisTemplate, redisService);
            }

            return repository;
        }

        /**
         * 获取具体的实现类的class
         * @param metadata
         * @return
         */
        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
                return BaseQueryDslJpaRepository.class;
            } else {
                return BaseJpaRedisRepositoryImpl.class;
            }
        }

        /**
         * Returns whether the given repository interface requires a QueryDsl specific implementation to be chosen.
         *
         * @param repositoryInterface
         * @return
         */
        private boolean isQueryDslExecutor(Class<?> repositoryInterface) {
            return QUERY_DSL_PRESENT && QueryDslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
        }
    }
}
