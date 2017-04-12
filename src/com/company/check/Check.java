package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public abstract class Check {
    private String id;
    private String queryText;
    private String name;
    private String messageText;
    private CheckType type;
    private ValidationMethod validationMethod;

    private static final String TEMP_TABLE_PLACEHOLDER = "%tempTable%";
    private static final String TARGET_TABLE_PLACEHOLDER = "%targetTable%";
    private static final String COLUMN_NAME_PLACEHOLDER = "%columnName%";


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

    public String getId() {
        return id;
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

        if (id != null ? !id.equals(check.id) : check.id != null) return false;
        if (queryText != null ? !queryText.equals(check.queryText) : check.queryText != null) return false;
        if (name != null ? !name.equals(check.name) : check.name != null) return false;
        if (messageText != null ? !messageText.equals(check.messageText) : check.messageText != null) return false;
        if (type != check.type) return false;
        return validationMethod == check.validationMethod;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (queryText != null ? queryText.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (messageText != null ? messageText.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (validationMethod != null ? validationMethod.hashCode() : 0);
        return result;
    }

    public abstract boolean validate(int resultCount, long numberOfRowsInTempTable);

    @Override
    public String toString() {
        return "Check{" +
                "id='" + id + '\'' +
                ", queryText='" + queryText + '\'' +
                ", name='" + name + '\'' +
                ", messageText='" + messageText + '\'' +
                ", type=" + type +
                ", validationMethod=" + validationMethod +
                '}';
    }

    public static Check create(String id, String queryText, String name, String messageText, CheckType type, ValidationMethod validationMethod) {
        Check check;
        if (ValidationMethod.ALL.equals(validationMethod)) {
            check = new AllValidationCheck(id, queryText, name, messageText, type);
        } else if (ValidationMethod.ZERO.equals(validationMethod)) {
            check = new ZeroValidationCheck(id, queryText, name, messageText, type);
        } else {
            throw new RuntimeException("Unknown validation method `" + validationMethod + "\'");
        }
        return check;
    }
}
