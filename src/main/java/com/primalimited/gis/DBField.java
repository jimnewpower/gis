package com.primalimited.gis;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

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

    public String toJSON() {
        if (value instanceof Number number) {
            return JSONValue.toJSONString(new NumericData(name, type, number));
        }
        if (value instanceof String string) {
            return JSONValue.toJSONString(new StringData(name, type, string));
        }
        return JSONValue.toJSONString(this);
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

    private static class NumericData {
        private final String name;
        private final String type;
        private final Number value;
        NumericData(String name, String type, Number value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "NumericData{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NumericData that = (NumericData) o;
            return name.equals(that.name) && type.equals(that.type) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, value);
        }

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }

        public Number value() {
            return value;
        }
    }

    private static class StringData {
        private final String name;
        private final String type;
        private final String value;
        StringData(String name, String type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "StringData{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringData that = (StringData) o;
            return name.equals(that.name) && type.equals(that.type) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type, value);
        }

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }

        public String value() {
            return value;
        }
    }
}
