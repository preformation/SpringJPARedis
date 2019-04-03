package com.cx.web;

import com.cx.utils.Const;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Alan Shu.
 * Date time 2019/3/21 21:45
 */
public abstract class BaseController<C> {

    public String apikey(String methodname, String[] paramnames, Object[] paramvals){
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return Const.apikey(((Class<C>)params[0]).getSimpleName(), methodname, paramnames, paramvals);
    }
}
