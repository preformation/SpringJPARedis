package com.cx.utils;

public class ObjWrapper<T> {

    private T data;

    public ObjWrapper() {
    }

    public ObjWrapper(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}