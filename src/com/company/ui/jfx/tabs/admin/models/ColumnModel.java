package com.company.ui.jfx.tabs.admin.models;

/**
 * Created by Александр on 15.04.2017.
 */
public class ColumnModel {

    String name;
    boolean editable;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnModel that = (ColumnModel) o;

        if (editable != that.editable) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (editable ? 1 : 0);
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
