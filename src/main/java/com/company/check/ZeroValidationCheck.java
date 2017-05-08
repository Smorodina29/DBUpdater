package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public class ZeroValidationCheck extends Check {


    public ZeroValidationCheck(String id, String queryText, String name, String messageText, CheckType type) {
        super(id, queryText, name, messageText, type, ValidationMethod.ZERO);
    }

    @Override
    public boolean validate(int resultCount, long numberOfRowsInTempTable) {
        return resultCount == 0;
    }
}
