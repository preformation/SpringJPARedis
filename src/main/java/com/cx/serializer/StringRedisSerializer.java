package com.cx.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

/**
 * 必须重写序列化器，否则@Cacheable注解的key会报类型转换错误
 *
 * @authors Alan
 */
public class StringRedisSerializer implements RedisSerializer<Object> {
 
    private final Charset charset;
 
    private final String target = "\"";
 
    private final String replacement = "";
 
    public StringRedisSerializer() {
        this(Charset.forName("UTF8"));
    }
 
    public StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }
 
    @Override
    public String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }
 
    @Override
    public byte[] serialize(final Object object) {
        String string = JSON.toJSONString(object, SerializerFeature.WriteMapNullValue);
        if (string == null) {
            return null;
        }
        string = string.replace(target, replacement);
        return string.getBytes(charset);
    }
}