package com.company.ui.jfx.tabs.admin.models;

import com.company.check.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 15.04.2017.
 */
public class ColumnModel {

    String name;
    String table;
    boolean editable;
    List<Check> checks = new ArrayList<>();

    int forUpdateId;

    public ColumnModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Check> getChecks() {
        return checks;
    }

    public void setChecks(List<Check> checks) {
        this.checks = checks;
    }

    public int getForUpdateId() {
        return forUpdateId;
    }

    public void setForUpdateId(int forUpdateId) {
        this.forUpdateId = forUpdateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnModel that = (ColumnModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return table != null ? table.equals(that.table) : that.table == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (table != null ? table.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ColumnModel{" +
                "name='" + name + '\'' +
                ", editable=" + editable +
                '}';
    }
}
