package com.company.check;

/**
 * Created by Александр on 06.01.2017.
 */
public class RefExistenceCheck extends AllValidationCheck {
    String sql = "select distinct u.%s from %s join %s r on u.%s = r.%s";

    @Override
    public CheckType getType() {
        return CheckType.ERROR;
    }

    @Override
    public String getName() {
        return "HZ";
    }

    @Override
    public String getSqlQuery(String tempTableName, String targetTableName, String columnName) {
        return String.format(sql, tempTableName, targetTableName, columnName, columnName);
    }

    @Override
    public String getSql() {
        return sql;
    }
}
