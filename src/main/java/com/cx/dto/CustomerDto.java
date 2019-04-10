package com.cx.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2019/3/1
 * @Version: 1.0
 */
@Data//@Data 包含了 @ToString、@EqualsAndHashCode、@Getter/@Setter和@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "children" })
public class CustomerDto implements Serializable {
    private static final long serialVersionUID = -2471128464535196140L;

    private Long id;
    private String name;
    private String email;
    private Date createdTime;

    public CustomerDto(String name, String email, Date createdTime) {
        this.name = name;
        this.email = email;
        this.createdTime = createdTime;
    }
}
