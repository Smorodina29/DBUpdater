package com.company.check;

/**
 * Created by Александр on 18.03.2017.
 */
public abstract class Check {
    public abstract CheckType getType();
    public abstract String getName();
    public abstract String getSql();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Check that = (Check) o;

        if (getSql() != null ? !getSql().equals(that.getSql()) : that.getSql() != null) return false;
        return getName() != null ? getName().equals(that.getName()) : that.getName() == null;

    }

    @Override
    public int hashCode() {
        int result = getSql() != null ? getSql().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }

    public abstract boolean validate(int resultCount, long numberOfRowsInTempTable);
}
