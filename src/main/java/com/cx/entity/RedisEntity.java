package com.cx.entity;

import com.cx.utils.Identifier;

import java.io.Serializable;

/**
 * Created by Alan.
 */
public interface RedisEntity<ID extends Serializable> extends Identifier<ID> {

}
