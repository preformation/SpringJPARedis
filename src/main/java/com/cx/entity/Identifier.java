package com.cx.entity;

import java.io.Serializable;

/**
 * Created by Alan Shu
 */
public interface Identifier<ID extends Serializable> {

    ID getId();
}
