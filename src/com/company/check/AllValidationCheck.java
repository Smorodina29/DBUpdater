package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public class AllValidationCheck extends Check {


    public AllValidationCheck(String queryText, String name, String messageText, CheckType type) {
        super(queryText, name, messageText, type, ValidationMethod.ALL);
    }

    @Override
    public boolean validate(int resultCount, long numberOfRowsInTempTable) {
        return resultCount == numberOfRowsInTempTable;
    }
}
