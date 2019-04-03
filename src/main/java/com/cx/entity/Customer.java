package com.cx.entity;

import com.cx.entity.generator.IdGenerate;
import com.cx.utils.Const;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
@Entity(name = "customer")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@Data//@Data 包含了 @ToString、@EqualsAndHashCode、@Getter/@Setter和@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "children" })
public class Customer implements RedisEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String surname;
    private Integer sex;
    private String email;
    private Date createdTime;

    public Customer(String name, String surname, Integer sex, String email, Date createdTime) {
        this.name = name;
        this.surname = surname;
        this.sex = sex;
        this.email = email;
        this.createdTime = createdTime;
    }
}
