package com.cx.service.impl;

import com.cx.dto.CustomerDto;
import com.cx.entity.Customer;
import com.cx.repository.CustomerRepository;
import com.cx.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.mapper.BeanMapper;

import java.util.List;


/**
 * 第三种方案
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/2
 * @Version: 1.0
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * @Author: 舒建辉
     * @Description:
     * @Date: Created on 2018/2/2
     * @Version: 1.0
     */
    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findById(Long id) {
        return customerRepository.findOne(id);
    }

    @Override
    public List<Customer> findBySex(Integer sex) {
        return customerRepository.findBySex(sex);
    }

    @Override
    public List<Customer> findByName(String name) {
        return customerRepository.findByName(name);
    }

    @Override
    public List<Customer> findByNameOrSurname(String name, String surname) {
        return customerRepository.findByNameOrSurname(name, surname);
    }

    @Override
    public Integer delete(Long id) {
        customerRepository.delete(id);
        return 1;
    }

    @Override
    public CustomerDto findByCustomerId(Long id) {
        return customerRepository.findByCustomerId(id);
    }

    @Override
    public List<CustomerDto> findByTaskIds(List<Long> ids) {
        return customerRepository.findByTaskIds(ids);
    }

    @Override
    public List<Long> findByCustomerIds(List<Long> ids) {
        return customerRepository.findByCustomerIds(ids);
    }

    @Override
    public List<CustomerDto> findCustomerDtoByIds(List<Long> ids) {
        List<Customer> entities = customerRepository.findAll(ids);
        return BeanMapper.mapList(entities, Customer.class, CustomerDto.class);
    }
}
