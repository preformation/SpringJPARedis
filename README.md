SpringBoot 开源企业级JPA REDIS无缝整合精细化缓存
======


# 前言

本项目发展自2019年，内部版本已在线上生产环境中使用，经过严格测试，BUG极少，现有的几种缓存方式都有demo，大家可以持续关注，有时间会不断更新，也希望大家积极参与共同开发。
大家有好的建议可以联系本人，一定采纳学习。

```
第一种方案：只做redis缓存，不保存Mysql数据库, 需加@EnableRedisRepositories注解

优点：配置极省，非常方便，功能强大，支持缓存加索引等

弊端：只能走Redis，不走Mysql，不适合我们通用的开发场景，只适用于以Redis做唯一数据保存的数据库场景
```

```
第二种方案：加注解方式  需加@EnableCaching注解

优点：配置较省，较为方便

弊端：虽然走Redis，也走Mysql，但开发中容易出错，缓存删除范围过大，缓存条数过多；加缓存工作量偏大；
```

```
第三种方案（推荐）：

优点：走Redis，也走Mysql，配置超级省，最为方便，功能可以非常强大，定制化实现，灵活运用，精细化缓存，缓存条数最少，缓存删除范围最小，全动态数据变化的缓存变更，开发中第一层缓存和第二层缓存都支持，并支持事务。

弊端：目前大部分场景都已支持，当然事在人为，可以做到很全面，还可考量并发性能、排序方式、加索引的支持等等场景
```

### QQ交流
*  作者: 972153513

# 版本说明

*   重构优化，目前发行版本v1.0.RELEASE，做成Maven仓库的依赖包
```
Maven版：

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
    <dependency>
        <groupId>com.github.preformation</groupId>
        <artifactId>SpringJPARedis</artifactId>
        <version>v1.0.RELEASE</version>
    </dependency>
    
Gradle版：

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
    
    dependencies {
         implementation 'com.github.preformation:SpringJPARedis:v1.0.RELEASE'
    }
```   

## 运行必备环境：

*   JDK 8 181 +
*   Maven 3.3.9 +
*   MySql 5.7 + 或 MariaDB
*   SpringBoot 1.5.7.RELEASE + （可自己升级版本）

## 实现功能：

通用功能
*   Spring事务控制JPA和jedis事务原子性等
*   支持缓存加载与查询排序
*   支持Redis单机模式配置
*   支持Redis哨兵模式配置
*   支持Redis集群模式配置
*   支持数据库主键自增模式
*   支持分布式统一分发ID模式
*   支持HikariCP连接池
*   支持lombok方式

一层缓存实现（StringRedisTemplate）参看SpringJPARedisDemo项目
*   Controller接口层缓存，无须考虑排序问题，缓存条数可能过多，顶层数据变化比较大，尤其分页和数据展现形式具多样性，但好在接口不多
*   支持ProtoStuffRedisSerializer最快方式
*   缓存时间默认4天，不能动态进行数据变化的缓存变更

二层缓存实现（RedisTemplate<String, ?>）
*   JPA 通用增删改查无缝缓存整合（BaseJpaRedisRepositoryImpl）（完全实现）
*   JPA 复杂SQL支持无缝缓存整合（BaseQueryDslJpaRepository）（完全实现）
*   JPA FindBy模式支持无缝缓存整合（SecurecyPostProcessor）（完全实现）
*   JPA @Query注解模式支持无缝缓存整合 （完全实现）
*   支持ProtoStuffRedisSerializer最快方式
*   支持KryoRedisSerializer方式
*   支持FastJsonRedisSerializer方式
*   支持Jackson2JsonRedisSerializer方式
*   支持StringRedisSerializer方式
*   支持JPA和Redis事务整合方式
*   支持精细化缓存，缓存条数极少，缓存删除范围最小
*   支持全动态数据变化的缓存变更