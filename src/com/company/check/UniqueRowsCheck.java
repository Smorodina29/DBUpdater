package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class UniqueRowsCheck {
    //error
    String sql = "select count(*) from %s u left join %s a on a.%s = u.%s";
    private String name = "Проверка на уникальность записей.";

    public String getSql() {
        return sql;
    }

    public String getName() {
        return name;
    }
}