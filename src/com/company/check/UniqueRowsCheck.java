package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class UniqueRowsCheck extends AllValidationCheck {
    //error
    String sql = "select count(*) from %s u left join %s a on a.%s = u.%s";
    private String name = "Проверка на уникальность записей.";

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
        return CheckType.ERROR;
    }

    @Override
    public String getName() {
        return name;
    }

}