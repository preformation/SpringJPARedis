package com.cx.service;

import com.cx.dto.CustomerDto;
import com.cx.entity.Customer;

import java.util.List;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/2
 * @Version: 1.0
 */
public interface CustomerService {

    Customer save(Customer customer);
    List<Customer> findAll();
    Customer findById(Long id);
    List<Customer> findBySex(Integer sex);
    List<Customer> findByName(String name);
    List<Customer> findByNameOrSurname(String name, String surname);
    Integer delete(Long id);

    CustomerDto findByCustomerId(Long id);
    List<CustomerDto> findByTaskIds(List<Long> ids);
    List<Long> findByCustomerIds(List<Long> ids);
    List<CustomerDto> findCustomerDtoByIds(List<Long> ids);
}
