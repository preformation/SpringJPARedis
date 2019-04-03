package com.cx.config;

import com.cx.utils.Const;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @Author: 舒建辉
 * @Description:
 * @Date: Created on 2019/3/13
 * @Version: 1.0
 */
@Configuration
//@EnableCaching  //第二种方案：加注解方式  屏蔽后是第三种方案：缓存和数据库都保存
@EnableTransactionManagement
//@EnableRedisRepositories //第一种方案：只做redis缓存，不保存数据库  屏蔽后是第三种方案：缓存和数据库都保存
public class RedisTxConfiguration extends CachingConfigurerSupport {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(redisProperties.getPool().getMaxActive());
        jedisPoolConfig.setMaxIdle(redisProperties.getPool().getMaxIdle());
        jedisPoolConfig.setMinIdle(redisProperties.getPool().getMinIdle());
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getPool().getMaxWait());
        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(redisProperties.getHost());
        jedisConnectionFactory.setPort(redisProperties.getPort());
        jedisConnectionFactory.setPassword(redisProperties.getPassword());
        jedisConnectionFactory.setTimeout(redisProperties.getTimeout());
        jedisConnectionFactory.setDatabase(redisProperties.getDatabase());
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.afterPropertiesSet();

        //哨兵模式：Redis Sentinel监听主服务，再主服务发生故障时能够切换至从服务，将从服务升为主服务来保证故障恢复，使用该功能需要在JedisConnectionFactory设置RedisSentinelConfiguration属性，目前Jedis对Redis Sentinel提供支持。
//        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration() .master("mymaster")
//                .sentinel("127.0.0.1", 26379) .sentinel("127.0.0.1", 26380);
//        return new JedisConnectionFactory(sentinelConfig);
        return jedisConnectionFactory;
    }

    /**
     * @Author: 舒建辉
     * @Description: 使用Repository方式
     * @Date: Created on 2018/2/7
     * @Version: 1.0
     */
    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(factory);

        // 使用kryoRedisSerializer 替换默认序列化
        KryoRedisSerializer<Object> kryoRedisSerializer = new KryoRedisSerializer<Object>(Object.class);
        //Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        //GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        //FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 设置value的序列化规则和 key的序列化规则
        //RedisSerializer<Object> stringRedisSerializer = new StringRedisSerializer();
        ProtoStuffRedisSerializer<Object> protoStuffRedisSerializer = new ProtoStuffRedisSerializer<Object>(Object.class);
        redisTemplate.setKeySerializer(protoStuffRedisSerializer);
        redisTemplate.setValueSerializer(protoStuffRedisSerializer);
        redisTemplate.setHashKeySerializer(protoStuffRedisSerializer);
        redisTemplate.setHashValueSerializer(protoStuffRedisSerializer);

        // explicitly enable transaction support
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setUsePrefix(true);
//        cacheManager.setCacheNames(Arrays.asList(REDIS_KEYSPACE_PRE+"user",REDIS_KEYSPACE_PRE+"role"));
        //设置默认的过期时间，单位秒
        cacheManager.setDefaultExpiration(Const.REDIS_2ND_TTL);

        // 还可以使用下面的方法为指定的key设定过期时间，它将会在computeExpiration方法中用到
//        Map<String, Long> expires = new HashMap<>();
//        expires.put("cacheNameKey", 20L);
//        expires.put("myKey", 40L);
//        cacheManager.setExpires(expires);
        return cacheManager;
    }

    @Bean(name="hikariDataSource")
    @Qualifier("hikariDataSource")
    @ConfigurationProperties(prefix="spring.datasource.hikari")
    public DataSource hikariDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException{
        return new DataSourceTransactionManager(hikariDataSource());
    }


    /**
     * 重写缓存key生成策略，可根据自身业务需要进行自己的配置生成条件
     * @return
     */
//    @Bean
//    @Override
//    public KeyGenerator keyGenerator() {
//        return new KeyGenerator() {
//            @Override
//            public Object generate(Object target, Method method, Object... params) {
//                StringBuilder sb = new StringBuilder();
//                sb.append(Const.REDIS_KEY_PRE);
//                sb.append(target.getClass().getName());
//                sb.append(method.getName());
//                for (Object obj : params) {
//                    sb.append(obj.toString());
//                }
//                return sb.toString();
//            }
//        };
//    }
}
