package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public abstract class ZeroValidationCheck extends Check {

    @Override
    public boolean validate(int resultCount, long numberOfRowsInTempTable) {
        return resultCount == 0;
    }
}
