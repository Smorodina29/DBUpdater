package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class UniqueIdCheck extends AllValidationCheck {
    //error
    String sql = "select count(distinct id) from %s";

    String name = "Проверка на уникальность ID во временной таблице.";

    @Override
    public String getSqlQuery(String tempTableName, String targetTableName, String columnName) {
        return String.format(sql, tempTableName);
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
