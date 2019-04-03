package com.cx.service.impl;

import com.cx.entity.User;
import com.cx.repository.UserRepository;
import com.cx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cx.utils.Const.REDIS_2ND_KEY_PRE;


/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/2
 * @Version: 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * @Author: 舒建辉
     * @Description: 因为jpa实现类中已加transactional，为了保证service对事物的控制，必须要有@Transactional
     * @Date: Created on 2018/2/2
     * @Version: 1.0
     */
    @Override
    @Transactional
    //第二种方案：加注解方式
    @Caching(
            evict = {
                @CacheEvict(value=REDIS_2ND_KEY_PRE+"user", key = "0"),
                @CacheEvict(value=REDIS_2ND_KEY_PRE+"user", key = "#user.id"),
                @CacheEvict(value=REDIS_2ND_KEY_PRE+"user", key = "#user.name"),
                @CacheEvict(value=REDIS_2ND_KEY_PRE+"user", key = "#user.sex")
            })
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    //第二种方案：加注解方式
    @Cacheable(value=REDIS_2ND_KEY_PRE+"user", key = "0")
    public List<User> findAll() {
        List<User> users = userRepository.findAll();
        return users;
    }

    @Override
    //第二种方案：加注解方式
    @Cacheable(value=REDIS_2ND_KEY_PRE+"user", key = "#id")
    public User findById(Integer id) {
        User user = userRepository.findOne(id);
        return user;
    }

    @Override
    //第二种方案：加注解方式
    @Cacheable(value=REDIS_2ND_KEY_PRE+"user", key = "#sex")
    public List<User> findBySex(Integer sex) {
        List<User> users = userRepository.findBySex(sex);
        return users;
    }

    @Override
    //第二种方案：加注解方式
    @Cacheable(value=REDIS_2ND_KEY_PRE+"user", key = "#name")
    public List<User> findByName(String name) {
        List<User> users = userRepository.findByName(name);
        return users;
    }

    @Override
    //第二种方案：加注解方式
    @CacheEvict(value=REDIS_2ND_KEY_PRE+"user", key = "#id", allEntries=true)
    public Integer delete(Integer id) {
        userRepository.delete(id);
        return 1;
    }
}
