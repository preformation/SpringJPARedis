package com.cx.repository;

import com.cx.entity.RedisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2019/3/18
 * @Version: 1.0
 */
@NoRepositoryBean
public interface BaseJpaRedisRepository<T extends RedisEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

}
