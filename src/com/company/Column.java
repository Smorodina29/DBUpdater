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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        if (isNullable != column.isNullable) return false;
        if (length != column.length) return false;
        if (name != null ? !name.equals(column.name) : column.name != null) return false;
        if (type != null ? !type.equals(column.type) : column.type != null) return false;
        return dataType == column.dataType;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (isNullable ? 1 : 0);
        result = 31 * result + length;
        return result;
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
