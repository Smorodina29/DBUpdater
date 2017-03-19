package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class PresentRowsCheck extends ZeroValidationCheck {

    //warning
    public String sql = "select count(*) from %s u join %s a on a.id = u.id where a.%s = u.%s";
    private String name = "Некоторые записи уже имеют целевое значение.";

    @Override
    public String getSqlQuery(String tempTableName, String targetTableName, String columnName) {
        return String.format(sql, tempTableName, targetTableName, columnName, columnName);
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public CheckType getType() {
        return CheckType.WARNING;
    }

    @Override
    public String getName() {
        return name;
    }

}
