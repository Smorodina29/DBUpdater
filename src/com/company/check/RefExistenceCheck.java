package com.company.check;

/**
 * Created by Александр on 06.01.2017.
 */
public class RefExistenceCheck {
    String sql = "select distinct u.%s from %s join %s r on u.%s = r.%s";

    public String getSql() {
        return sql;
    }
}
