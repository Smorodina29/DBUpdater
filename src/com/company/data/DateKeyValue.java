package com.company.data;

import com.company.DataType;

import java.util.Date;

/**
 * Created by Александр on 20.11.2016.
 */
public class DateKeyValue extends KeyValue<Date> {

    public static final DataType TYPE = DataType.DATETIME;

    public DateKeyValue(int key, Date value) {
        super(key, value);
    }

    public Date getValue() {
        return value;
    }
}
