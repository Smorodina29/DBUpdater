package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class PresentRowsCheck {

    //warning

    public String sql = "select count(*) from %s u join %S a on a.id = u.id where a.%s = u.%s";

    public String getSql() {
        return sql;
    }
}
