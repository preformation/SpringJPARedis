package com.cx.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * ProtoStuffSerializerUtil
 *
 * @author Alan Shu
 * @date 2019-3-18
 */
public class ProtoStuffRedisSerializer<T> implements RedisSerializer<T> {

    private Class<T> clazz;

    public ProtoStuffRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * 序列化对象
     * @param obj
     * @return
     */
    @Override
    public byte[] serialize(T obj) {
        if (obj == null) {
            throw new RuntimeException("序列化对象(" + obj + ")!");
        }
        @SuppressWarnings("unchecked")
        Schema<T> schema = (Schema<T>) RuntimeSchema.getSchema(obj.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(1024 * 1024);
        byte[] protostuff = null;
        try {
            protostuff = ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new RuntimeException("序列化(" + obj.getClass() + ")对象(" + obj + ")发生异常!", e);
        } finally {
            buffer.clear();
        }
        return protostuff;
    }

    /**
     * 反序列化对象
     * @param paramArrayOfByte
     * @return
     */
    @Override
    public T deserialize(byte[] paramArrayOfByte) {
        if (paramArrayOfByte == null || paramArrayOfByte.length == 0) {
            throw new RuntimeException("反序列化对象发生异常,byte序列为空!");
        }
        T instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException  e1) {
            throw new RuntimeException("反序列化过程中依据类型创建对象失败!", e1);
        } catch(IllegalAccessException e2){
            throw new RuntimeException("反序列化过程中依据类型创建对象失败!", e2);
        }
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        ProtostuffIOUtil.mergeFrom(paramArrayOfByte, instance, schema);
        return instance;
    }
}