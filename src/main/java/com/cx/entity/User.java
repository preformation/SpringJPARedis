package com.cx.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2019/3/9
 * @Version: 1.0
 */
@Entity(name = "cx_user")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@Data//@Data 包含了 @ToString、@EqualsAndHashCode、@Getter/@Setter和@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "children" })
//@RedisHash(timeToLive= Const.REDIS_TTL)  //第一种方案：只做redis缓存，不保存数据库
public class User {
    //@org.springframework.data.annotation.Id //第一种方案：只做redis缓存，不保存数据库
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    //第一种方案：只做redis缓存，不保存数据库
    //@Indexed //创建redis缓存索引
    private String name;
    private String surname;
    //第一种方案：只做redis缓存，不保存数据库
    //@Indexed //如果要按照http://localhost:8088/user/findBySex/3 改路径查寻，必须要在该字段加索引
    private Integer sex;
    private String email;

//    private @GeoIndexed Point location;

    public User(String name, String surname, Integer sex, String email) {
        this.name = name;
        this.surname = surname;
        this.sex = sex;
        this.email = email;
    }
}
