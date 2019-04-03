package com.cx.config;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;
 
/**
 * @param <T>
 * @author 舒建辉
 */
public class KryoRedisSerializer<T> implements RedisSerializer<T> {
    private static final Logger logger = LoggerFactory.getLogger(KryoRedisSerializer.class);

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final ThreadLocal<Kryo> kryos = ThreadLocal.withInitial(Kryo::new);

    private Class<T> clazz;

    public KryoRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return EMPTY_BYTE_ARRAY;
        }

        Kryo kryo = kryos.get();
        kryo.setReferences(false);
        kryo.register(clazz);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Output output = new Output(baos)) {
            if (clazz != null && kryo.isFinal(clazz)) {
                Serializer serializer = kryo.getSerializer(clazz);
                kryo.writeObjectOrNull(output, t, serializer);
            } else
                kryo.writeClassAndObject(output, t);
            output.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return EMPTY_BYTE_ARRAY;
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        Kryo kryo = kryos.get();
        kryo.setReferences(false);
        kryo.register(clazz);

        try (Input input = new Input(bytes)) {
            if (clazz != null && kryo.isFinal(clazz)) {
                Serializer serializer = kryo.getSerializer(clazz);
                return (T) kryo.readObjectOrNull(input, clazz, serializer);
            } else
                return (T) kryo.readClassAndObject(input);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}