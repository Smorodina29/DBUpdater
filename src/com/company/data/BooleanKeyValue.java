package com.company.data;

import com.company.DataType;


/**
 * Created by Александр on 20.11.2016.
 */
public class BooleanKeyValue extends KeyValue<Boolean> {

    public static final DataType TYPE = DataType.BOOLEAN;

    public BooleanKeyValue(Boolean value) {
        super(value);
    }

    public BooleanKeyValue(int key, Boolean value) {
        super(key, value);
    }

    public Boolean getValue() {
        return value;
    }
}
