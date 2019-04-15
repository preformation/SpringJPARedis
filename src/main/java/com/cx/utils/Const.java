package com.cx.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alan Shu.
 * Date time 2019/3/12 21:57
 */
public class Const {

    private Const(){}

    /**
     * keyspace前缀，一般为项目名称+ ":" + 第1层缓存
     */
    public static final String REDIS_1ST_KEY_PRE = "sjr:cache1st:";
    /**
     * keyspace前缀，一般为项目名称+ ":" + 第2层缓存
     */
    public static final String REDIS_2ND_KEY_PRE = "sjr:cache2nd:";

    /**
     * 如果给一个属性明确的添加了@TimeToLive 注解，它将从Redis 读取真实的TTL 或PTTL 值。-1 则表示该object 没有关联过期时间。
     */
    public static final long REDIS_1ST_TTL= 345600; //过期时间4天

    public static final long REDIS_2ND_TTL= -1;

    /**
     * api层缓存key
     * @param methodname
     * @param paramnames
     * @return
     */
    public static String apikey(String classname, String methodname, String[] paramnames, Object[] paramvals){
        StringBuffer sb = new StringBuffer(REDIS_1ST_KEY_PRE + classname);
        sb.append(":apis").append(StringUtils.isBlank(methodname)? "" : ":"+methodname);
        if(ArrayUtils.isNotEmpty(paramnames) && ArrayUtils.isNotEmpty(paramvals)) {
            Arrays.stream(paramnames).forEach(fieldname -> {
                sb.append(":"+fieldname);
            });
            Arrays.stream(paramvals).forEach(paramval -> {
                if(ObjectUtils.isEmpty(paramval)){
                    sb.append(":0");
                }else {
                    if (paramval instanceof List) {
                        Collections.sort((List) paramval);
                    }
                    if (paramval instanceof Object[]) {
                        Arrays.sort((Object[]) paramval);
                    }
                    sb.append(":" + paramval.hashCode());
                }
            });
        }

        return sb.toString();
    }
}
