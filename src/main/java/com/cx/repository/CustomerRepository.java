package com.cx.repository;

import com.cx.dto.CustomerDto;
import com.cx.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select new com.cx.dto.CustomerDto(id,name,email,createdTime) from customer where id = (?1)")
    CustomerDto findByCustomerId(@Param("id") Long id);

    @Query("select new com.cx.dto.CustomerDto(id,name,email,createdTime) from customer where id IN (?1) and sex <> 3")
    List<CustomerDto> findByTaskIds(@Param("ids") List<Long> ids);

    @Query(value = "select id from customer where id IN (:ids) and sex != 3",nativeQuery = true)
    List<Long> findByCustomerIds(@Param("ids") List<Long> ids);
}
