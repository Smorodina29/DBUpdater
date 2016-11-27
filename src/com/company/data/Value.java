package com.company.data;

/**
 * Created by Александр on 20.11.2016.
 */
public class Value<T> {

    T value;

    public Value(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
