SpringBoot 开源企业级JPA REDIS无缝整合精细化缓存
======


# 前言

本项目发展自2019年，最新版已在线上生产环境中使用，现有的几种缓存方式都有demo，大家可以持续关注，有时间会不断更新，也希望大家积极参与共同开发。
大家有好的建议可以联系本人，一定采纳学习。

```
第一种方案：只做redis缓存，不保存Mysql数据库, 需加@EnableRedisRepositories注解

优点：配置极省，非常方便，功能强大，支持缓存加索引等

弊端：只能走Redis，不走Mysql，不适合我们通用的开发场景，只适用于以Redis做唯一数据保存的数据库场景
```

```
第二种方案：加注解方式  需加@EnableCaching注解

优点：配置极省，较为方便

弊端：虽然走Redis，也走Mysql，但开发中容易出错，缓存删除范围过大，缓存条数过多；加缓存工作量偏大；
```

```
第三种方案（推荐）：

优点：走Redis，也走Mysql，配置超极省，最为方便，功能可以非常强大，定制化实现，灵活运用，精细化缓存，缓存条数最少，缓存删除范围最小，全动态数据变化的缓存变更，开发中第一层缓存和第二层缓存都支持，并支持事务。

弊端：目前支持的场景不够全面，当然事在人为，可以做到很全面，还需考量并发性能、排序方式、加索引、不同JPA开发方式的支持等等场景
```

### QQ交流
*  作者: 972153513

# 版本说明

*   初步非模块化无在线依赖包 1.0-SNAPSHOT 版本

# 本版说明(v1.0-SNAPSHOT)

## 运行必备环境：

*   JDK 8 181 + 或 OpenJDK 11 +
*   Maven 3.5.3 +
*   Redis 4.0.8 +
*   MySql 5.7 + 或 MariaDB
*   SpringBoot 1.5.7.RELEASE + （可自己升级版本）

## 实现功能：

一层缓存实现（StringRedisTemplate）
*   Controller接口层缓存，无须考虑排序问题，缓存条数可能过多，顶层数据变化比较大，尤其分页和数据展现形式具多样性
*   支持ProtoStuffRedisSerializer最快方式
*   缓存时间默认4天，不能动态进行数据变化的缓存变更


二层缓存实现（RedisTemplate<String, ?>）
*   JPA 通用增删改查无缝缓存整合（BaseJpaRedisRepositoryImpl）（已经实现）
*   JPA 复杂SQL支持无缝缓存整合（BaseQueryDslJpaRepository）（尚未完全实现）
*   JPA FindBy模式支持无缝缓存整合（SecurecyPostProcessor）（已经实现）
*   支持ProtoStuffRedisSerializer最快方式
*   支持KryoRedisSerializer方式
*   支持FastJsonRedisSerializer方式
*   支持Jackson2JsonRedisSerializer方式
*   支持StringRedisSerializer方式
*   支持JPA和Redis事务整合方式
*   支持精细化缓存，缓存条数极少，缓存删除范围最小
*   支持全动态数据变化的缓存变更