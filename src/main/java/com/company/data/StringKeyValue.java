package com.company.data;

import com.company.DataType;

/**
 * Created by Александр on 20.11.2016.
 */
public class StringKeyValue extends KeyValue<String> {

    public static final DataType TYPE = DataType.VARCHAR;

    public StringKeyValue(String value) {
        super(value);
    }

    public StringKeyValue(int key, String value) {
        super(key, value);
    }

    public String getValue() {
        return  value;
    }
}
