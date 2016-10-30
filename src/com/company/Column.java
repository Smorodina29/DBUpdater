package com.company;

/**
 * Created by Александр on 02.10.2016.
 */
public class Column {

    String name;
    String type;
    boolean isNullable;
    int length;

    Column(String name, String type, boolean isNullable, int length){
        this.name = name;
        this.type = type;
        this.isNullable = isNullable;
        this.length = length;
    }

    @Override
    public String toString() {
        return  "Column{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isNullable=" + isNullable +
                ", length=" + length +
                '}';
    }
}
