package com.company.data;

import com.company.DataType;

/**
 * Created by Александр on 20.11.2016.
 */
public class FloatKeyValue extends KeyValue<Float> {

    public static final DataType TYPE = DataType.FLOAT;

    public FloatKeyValue(Float value) {
        super(value);
    }

    public FloatKeyValue(int key, Float value) {
        super(key, value);
    }

    public Float getValue() {
        return value;
    }
}
