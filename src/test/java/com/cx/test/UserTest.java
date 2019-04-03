package com.cx.test;

import com.cx.entity.User;
import com.cx.repository.UserRepository;
import com.cx.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
//@ActiveProfiles("test")
//@WebAppConfiguration
public class UserTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Test
    public void test01() {
        User tom = userRepository.save(new User("Tom", "t", 1, "Tom@cx.com"));
        System.out.println(tom);
    }

    @Test
    public void test02() {
        User user = userRepository.findOne(2);
        System.out.println(user);
        if (null != user) {
//            System.out.println(user.getAddresses());
        }
    }

    @Test
    public void test04() {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.<String>get("name"), "Aa");
            }
        };
        long one = System.currentTimeMillis();
        Page<User> userPage = userRepository.findAll(specification, new PageRequest(0, 10));
        long two = System.currentTimeMillis();
        Page<User> userPage2 = userRepository.findAll(specification, new PageRequest(0, 10));
        long three = System.currentTimeMillis();
        System.out.println(" first : " + (two - one));
        System.out.println(" two : " + (three - two));
        List<User> users = userPage.getContent();
        System.out.println(users);
    }

    @Test
    public void test08() {
        List<User> users = userRepository.findByNameOrSurname("a", "a");
        for (User user : users) {
            System.out.println(user);
        }
    }

    @Test
    public void test20() {
        User user = userService.save(new User("Tom", "t", 1, "Tom@cx.com"));
        System.out.println(user);
    }

    @Test
    public void test29() {
        User user = new User("Aa", "a", 1, "Aa@cx.com");
        for (int i = 10; i < 10000; i++) {
            user.setId(null);
            userRepository.save(user);
        }
    }

    @Test
    public void test30() {
        Long oneSum = 0l;
        for (int i = 1; i <= 5000; i++) {
            long beginOne = System.currentTimeMillis();
            User one = userRepository.findOne(i);
            long endOne = System.currentTimeMillis();
            oneSum += (endOne - beginOne);
        }
        System.out.println("findOne 5000次 共耗时: " + oneSum + "平均值 : " + (oneSum / 5000));
        Long twoSum = 0l;
        for (int i = 5001; i <= 10000; i++) {

            long beginTwo = System.currentTimeMillis();
            User two = userRepository.findById(i);
            long endTwo = System.currentTimeMillis();
            twoSum += (endTwo - beginTwo);
        }
        System.out.println("findById 5000次 共耗时: " + twoSum + "平均值 : " + (twoSum / 5000));
    }

    @Test
    public void test31() {
        Specification<User> specification = new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.<String>get("name"), "Aa");
            }
        };
        long beginTemp = System.currentTimeMillis();
        List<User> temp = userRepository.findAll(specification);//存入一级缓存，避免影响后续查询时效不平等
        long endTemp = System.currentTimeMillis();
        System.out.println("first 共耗时: " + (endTemp - beginTemp));
        Long twoSum = 0l;
        for (int i = 0; i < 1000; i++) {
            long beginTwo = System.currentTimeMillis();
            List<User> users = userRepository.findByName("Aa");
            long endTwo = System.currentTimeMillis();
            twoSum += (endTwo - beginTwo);
        }
        System.out.println("findByName 1000次 共耗时: " + twoSum + "平均值 : " + (twoSum / 1000));
        Long oneSum = 0l;
        for (int i = 0; i < 1000; i++) {
            long beginOne = System.currentTimeMillis();
            List<User> users = userRepository.findAll(specification);
            long endOne = System.currentTimeMillis();
            oneSum += (endOne - beginOne);
        }
        System.out.println("findAll 1000次 共耗时: " + oneSum + "平均值 : " + (oneSum / 1000));
    }
}
