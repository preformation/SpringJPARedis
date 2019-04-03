package com.cx.service;

import com.cx.entity.User;
import java.util.List;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/2
 * @Version: 1.0
 */
public interface UserService {

    User save(User user);
    List<User> findAll();
    User findById(Integer id);
    List<User> findBySex(Integer sex);
    List<User> findByName(String name);
    Integer delete(Integer id);
}
