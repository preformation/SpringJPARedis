package com.cx.web;

import com.cx.dto.CustomerDto;
import com.cx.entity.Customer;
import com.cx.service.CustomerService;
import com.cx.service.impl.RedisService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
@RestController
@RequestMapping("/customer")
public class CustomerController extends BaseController<CustomerController> {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private RedisService redisService;

    @RequestMapping("/index")
    public String index() {
        Date now = new Date();
        customerService.save(new Customer((int)Math.random()+(char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com", now));
        customerService.save(new Customer((char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97)+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com", now));
        customerService.save(new Customer((int)Math.random()+ "" +(int)Math.random()+(char)(int)(Math.random()*26+97), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com", now));
        customerService.save(new Customer((char)(int)(Math.random()*26+97)+ "" +(int)Math.random()+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com", now));
        customerService.save(new Customer((char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97)+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com", now));
        return "ok";
    }

    @RequestMapping("/update")
    public String update() {
        List<Customer> customers = findAll();
        if(null != customers && !customers.isEmpty()) {
            Customer customer = customers.get(0);
            customer.setName((int) Math.random() + (char) (int) (Math.random() * 26 + 97) + "" + (char) (int) (Math.random() * 26 + 97));
            customerService.save(customer);
        }
        return "ok";
    }

    @RequestMapping("/findAll")
    public List<Customer> findAll() {
//        String apikey = apikey("findAll", null, null);
//        List<Customer> customers = redisService.getListCache(apikey, Customer.class);
//        if(!CollectionUtils.isEmpty(customers)) {
//            return customers;
//        }

        List<Customer> customers = customerService.findAll();
//        redisService.putListCacheWithExpireTime(apikey, customers, Const.REDIS_1ST_TTL);
        return CollectionUtils.isEmpty(customers)?Lists.newArrayListWithCapacity(0):customers;
    }

    @RequestMapping("/findById/{id}")
    public Customer findById(@PathVariable Long id) {
        Customer customer = customerService.findById(id);
        return customer;
    }

    @RequestMapping("/findBySex/{sex}")
    public List<Customer> findBySex(@PathVariable Integer sex) {
//        String apikey = apikey("findBySex", new String[]{"sex"}, new Object[]{sex});
//        List<Customer> customers = redisService.getListCache(apikey, Customer.class);
//        if(!CollectionUtils.isEmpty(customers)) {
//            return customers;
//        }

        List<Customer> customers = customerService.findBySex(sex);
//        redisService.putListCacheWithExpireTime(apikey, customers, Const.REDIS_1ST_TTL);
        return CollectionUtils.isEmpty(customers)?Lists.newArrayListWithCapacity(0):customers;
    }

    @RequestMapping("/findByName/{name}")
    public List<Customer> findByName(@PathVariable String name) {
//        String apikey = apikey("findByName", new String[]{"name"}, new Object[]{name});
//        List<Customer> customers = redisService.getListCache(apikey, Customer.class);
//        if(!CollectionUtils.isEmpty(customers)) {
//            return customers;
//        }

        List<Customer> customers = customerService.findByName(name);
//        redisService.putListCacheWithExpireTime(apikey, customers, Const.REDIS_1ST_TTL);
        return CollectionUtils.isEmpty(customers)?Lists.newArrayListWithCapacity(0):customers;
    }

    @RequestMapping("/findByNameOrSurname/{name}/{surname}")
    public List<Customer> findByNameOrSurname(@PathVariable String name, @PathVariable String surname) {
//        String apikey = apikey("findByNameOrSurname", new String[]{"name", "surname"}, new Object[]{name, surname});
//        List<Customer> customers = redisService.getListCache(apikey, Customer.class);
//        if(!CollectionUtils.isEmpty(customers)) {
//            return customers;
//        }

        List<Customer> customers = customerService.findByNameOrSurname(name, surname);
        //redisService.putListCacheWithExpireTime(apikey, customers, Const.REDIS_1ST_TTL);
        return CollectionUtils.isEmpty(customers)?Lists.newArrayListWithCapacity(0):customers;
    }

    @RequestMapping("/delete/{id}")
    public Integer delete(@PathVariable Long id) {
        customerService.delete(id);
        return 1;
    }

    @RequestMapping("/findByTaskId")
    public List<CustomerDto> findByTaskId() {
        List<Long> ids = Lists.newArrayListWithCapacity(4);
        ids.add(85l);
        ids.add(86l);
        ids.add(87l);
        ids.add(88l);
        return customerService.findByTaskId(ids);
    }

    @RequestMapping("/findByCustomerId")
    public List<Long> findByCustomerId() {
        List<Long> ids = Lists.newArrayListWithCapacity(4);
        ids.add(85l);
        ids.add(86l);
        ids.add(87l);
        ids.add(88l);
        return customerService.findByCustomerId(ids);
    }

    /**
     * 查找
     * @return
     */
    @RequestMapping("/findCustomerDtoByIds")
    public List<CustomerDto> findCustomerDtoByIds() {
        List<Long> ids = Lists.newArrayListWithCapacity(4);
        ids.add(85l);
        ids.add(86l);
        ids.add(87l);
        ids.add(88l);
        return customerService.findCustomerDtoByIds(ids);
    }

}
