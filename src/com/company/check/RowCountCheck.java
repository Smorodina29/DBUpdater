package com.company.check;

/**
 * Created by Александр on 18.12.2016.
 */
public class RowCountCheck {
    //error
    String sql1 = "select count(*) from %s";
    String sql2 = "select count(distinct id) from %s";

    String name = "Проверка на уникальность ID во временной таблице.";

    public String getSql1() {
        return sql1;
    }

    public void setSql1(String sql1) {
        this.sql1 = sql1;
    }

    public String getSql2() {
        return sql2;
    }

    public void setSql2(String sql2) {
        this.sql2 = sql2;
    }

    public String getName() {
        return name;
    }
}
