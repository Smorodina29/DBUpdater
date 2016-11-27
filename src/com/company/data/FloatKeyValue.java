package com.company.data;

import com.company.DataType;

/**
 * Created by Александр on 20.11.2016.
 */
public class FloatKeyValue extends KeyValue<Double> {

    public static final DataType TYPE = DataType.FLOAT;

    public FloatKeyValue(int key, Double value) {
        super(key, value);
    }

    public Double getValue() {
        return value;
    }
}
