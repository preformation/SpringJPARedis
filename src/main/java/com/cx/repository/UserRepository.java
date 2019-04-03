package com.cx.repository;

import com.cx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2018/2/1
 * @Version: 1.0
 */
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    /**
     * @Author: 舒建辉
     * @Description: 根据名称返回(eq) ps:spring data会根据方法名进行实现
     * @Date: Created on 2018/2/1
     * @Version: 1.0
     */
    List<User> findBySex(Integer sex);
    User findById(Integer id);
    List<User> findByName(String name);
    List<User> findByNameOrSurname(String name, String surname);

//    @Query(value = "select u from User u where u.id = ?1")
//    User findById(Integer id);
//
//    @Query(value = "select u from User u where u.id = :cx_id")
//    User tempHqlParam(@Param("cx_id") Integer id);
//
//    @Query(value = "select * from cx_user where id = ?1", nativeQuery = true)
//    User tempSql(Integer id);
//
//    @Query(value = "select * from cx_user where id = :cx_id", nativeQuery = true)
//    User tempSqlParam(@Param("cx_id") Integer id);
}
