package com.company.data;

/**
 * Created by Александр on 13.11.2016.
 */
public class KeyValue<T> {
    public int key;
    public T value;

    public KeyValue(int key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue<?> keyValue = (KeyValue<?>) o;

        if (key != keyValue.key) return false;
        return value != null ? value.equals(keyValue.value) : keyValue.value == null;

    }

    @Override
    public int hashCode() {
        int result = key;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value='" + value + '\'' +
                '}';
    }
}
