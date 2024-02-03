package com.sky.config;

public class A<T> {
    private T t;

    public void print(T t) {
        System.out.println(t.getClass());
    }
}
