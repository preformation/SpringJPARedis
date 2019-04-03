package com.cx.config;

import com.cx.utils.BeanHelper;
import com.cx.utils.JsonHelper;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static com.cx.utils.Const.REDIS_2ND_KEY_PRE;

@Component //标记为组件
public class SpringCacheKeyGenerator implements KeyGenerator {

    private final static int NO_PARAM_KEY = 0;
    private String keyPrefix = REDIS_2ND_KEY_PRE;// key前缀，用于区分不同项目的缓存，建议每个项目单独设置

    @Override
    public Object generate(Object target, Method method, Object... params) {
        char sp = ':';
        StringBuffer stringBuffer = new StringBuffer(keyPrefix);
        // 类名
        stringBuffer.append(target.getClass().getSimpleName());
        stringBuffer.append(sp);
        // 方法名
        stringBuffer.append(method.getName());
        stringBuffer.append(sp);
        if (params.length > 0) {
            // 参数值
            for (Object object : params) {
                if (BeanHelper.isSimpleValueType(object.getClass())) {
                    stringBuffer.append(object);
                } else {
                    stringBuffer.append(JsonHelper.serialize(object).hashCode());
                }
            }
        }else {
            stringBuffer.append(NO_PARAM_KEY);
        }
        return stringBuffer.toString();
    }



    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}