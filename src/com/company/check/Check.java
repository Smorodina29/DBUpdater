package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public abstract class Check {
    public String id;
    public String queryText;
    public String name;
    public String messageText;
    public CheckType type;
    public ValidationMethod validationMethod;

    public static final String TEMP_TABLE_PLACEHOLDER = "%tempTable%";
    public static final String TARGET_TABLE_PLACEHOLDER = "%targetTable%";
    public static final String COLUMN_NAME_PLACEHOLDER = "%columnName%";


    public Check(String id, String queryText, String name, String messageText, CheckType type, ValidationMethod validationMethod) {
        this.id = id;
        this.queryText = queryText;
        this.name = name;
        this.messageText = messageText;
        this.type = type;
        this.validationMethod = validationMethod;
    }

     public String getSqlQuery(String tempTableName, String targetTableName, String columnName){
         return getQueryText().replace(TEMP_TABLE_PLACEHOLDER, tempTableName).replace(TARGET_TABLE_PLACEHOLDER, targetTableName).replace(COLUMN_NAME_PLACEHOLDER, columnName);
     }

    public String getQueryText() {
        return queryText;
    }

    public String getName() {
        return name;
    }

    public String getMessageText() {
        return messageText;
    }

    public CheckType getType() {
        return type;
    }

    public ValidationMethod getValidationMethod() {
        return validationMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Check check = (Check) o;

        if (queryText != null ? !queryText.equals(check.queryText) : check.queryText != null) return false;
        if (type != check.type) return false;
        return validationMethod == check.validationMethod;

    }

    @Override
    public int hashCode() {
        int result = queryText != null ? queryText.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (validationMethod != null ? validationMethod.hashCode() : 0);
        return result;
    }

    public abstract boolean validate(int resultCount, long numberOfRowsInTempTable);
}
