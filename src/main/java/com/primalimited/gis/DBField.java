package com.primalimited.gis;

import java.util.Objects;

/**
 * Data representing one column from one row in the .dbf file.
 */
public class DBField {
    private final String name;
    private final String type;
    private final Object value;

    /**
     * Constructor.
     *
     * @param name the column name.
     * @param type the data type.
     * @param value the column value.
     */
    DBField(String name, String type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "DBRecord{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBField dbField = (DBField) o;
        return name.equals(dbField.name) && type.equals(dbField.type) && value.equals(dbField.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, value);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
