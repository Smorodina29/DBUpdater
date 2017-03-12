package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class PresentRowsCheck {

    //warning

    public String sql = "select count(*) from %s u join %s a on a.id = u.id where a.%s = u.%s";
    private String name = "Некоторые записи уже имеют целевое значение.";

    public String getSql() {
        return sql;
    }

    public String getName() {
        return name;
    }
}
