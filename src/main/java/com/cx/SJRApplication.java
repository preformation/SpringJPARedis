package com.cx;

import com.cx.repository.BaseRepositoryFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
@EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
@SpringBootApplication //(exclude= {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class SJRApplication {

    public static void main(String[] args) {
        SpringApplication.run(SJRApplication.class, args);
    }
}
