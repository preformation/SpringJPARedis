package com.cx.repository;

import com.cx.entity.Customer;

import java.util.List;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
public interface CustomerRepository extends BaseJpaRedisRepository<Customer, Long> {

    /**
     * @Author: 舒建辉
     * @Description: 根据名称返回(eq) ps:spring data会根据方法名进行实现
     * @Date: Created on 2018/2/1
     * @Version: 1.0
     */
    List<Customer> findBySex(Integer sex);
    List<Customer> findByName(String name);
    List<Customer> findByNameOrSurname(String name, String surname);

}
