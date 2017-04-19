package com.company.ui.jfx.tabs.admin.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 15.04.2017.
 */
public class TableModel {

    String name;
    List<ColumnModel> columns = new ArrayList<>();
    boolean addAllowed;

    public TableModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public boolean isAddAllowed() {
        return addAllowed;
    }

    public void setAddAllowed(boolean addAllowed) {
        this.addAllowed = addAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableModel that = (TableModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return columns != null ? columns.equals(that.columns) : that.columns == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (columns != null ? columns.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TableModel{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                '}';
    }
}
