package com.company;

/**
 * Created by Александр on 02.10.2016.
 */
public class Column {

    String name;
    String type;
    DataType dataType;
    boolean isNullable;
    int length;

    Column(String name, String type, DataType dataType, boolean isNullable, int length){
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.isNullable = isNullable;
        this.length = length;
    }

    @Override
    public String toString() {
        return "Column{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", dataType=" + dataType +
                ", isNullable=" + isNullable +
                ", length=" + length +
                '}';
    }
}
