package com.company.data;

import com.company.Column;

/**
 * Created by Александр on 22.01.2017.
 */
public class ColumnValue {

    public Column column;
    public String value;

    public ColumnValue(Column column, String value) {
        this.column = column;
        this.value = value;
    }
}
