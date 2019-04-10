package com.cx.utils;

import java.util.ArrayList;
import java.util.List;

public class ListWrapper<T> {

    private List<T> data = new ArrayList<>();

    public int size() {
        return data.size();
    }

    public ListWrapper() {
    }

    public ListWrapper(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}