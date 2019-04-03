package com.cx.web;

import com.cx.entity.User;
import com.cx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String index() {
        char c=(char)(int)(Math.random()*26+97);
        char c1 = (char)(int)(Math.random()*26+97);
        String c2 = c1+ "" +c;
        userService.save(new User((int)Math.random()+(char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com"));
        userService.save(new User((char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97)+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com"));
        userService.save(new User((int)Math.random()+ "" +(int)Math.random()+(char)(int)(Math.random()*26+97), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com"));
        userService.save(new User((char)(int)(Math.random()*26+97)+ "" +(int)Math.random()+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com"));
        userService.save(new User((char)(int)(Math.random()*26+97)+ "" +(char)(int)(Math.random()*26+97)+(int)Math.random(), (char)(int)(Math.random()*26+97)+""+(int)Math.random(), new Random().nextInt(3)+1, (char)(int)(Math.random()*26+97)+(char)(int)(Math.random()*26+97)+(int)Math.random() + "@cx.com"));
        return "ok";
    }

    @RequestMapping("/findAll")
    public List<User> findAll() {
        List<User> users = userService.findAll();
        return users;
    }

    @RequestMapping("/findById/{id}")
    public User findById(@PathVariable Integer id) {
        User user = userService.findById(id);
        return user;
    }

    @RequestMapping("/findBySex/{sex}")
    public List<User> findBySex(@PathVariable Integer sex) {
        List<User> users = userService.findBySex(sex);
        return users;
    }

    @RequestMapping("/findByName/{name}")
    public List<User> findByName(@PathVariable String name) {
        List<User> users = userService.findByName(name);
        return users;
    }

    @RequestMapping("/delete/{id}")
    public Integer delete(@PathVariable Integer id) {
        userService.delete(id);
        return 1;
    }
}
