package com.primalimited.gis;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holder for all data in the .dbf file. Not recommended to use this as it could definitely cause
 * an OutOfMemory exception for larger shapefile .dbf files.
 */
public class DBASETableData {
    public static final double DVAL_DOUBLE = 3.4E+38;

    private Object[][] data;
    private int nRecords, nFields;
    private List<String> columnNames = new ArrayList<>();
    private FieldType[] fieldTypeArray;

    /**
     * Default constructor.
     *
     * @param data the table data.
     * @param nRecords number of rows in the table.
     * @param nFields number of columns in the table.
     * @param columnNamesArray names of the table columns.
     */
    public DBASETableData(
            Object[][] data,
            int nRecords,
            int nFields,
            String[] columnNamesArray
    ) {
        super();
        this.data = data;
        this.nRecords = nRecords;
        this.nFields = nFields;
        if (columnNamesArray != null)
            for (int index = 0; index < columnNamesArray.length; index++)
                this.columnNames.add(columnNamesArray[index]);
    }

    /**
     * @return column names.
     */
    public String[] getColumnNames() {
        if (columnNames == null)
            return(null);
        String[] names = new String[columnNames.size()];
        for (int index = 0; index < columnNames.size(); index++)
            names[index] = columnNames.get(index);
        return(names);
    }

    public String getColumnName(int index) {
        return columnNames.get(index);
    }

    public String[] getRealColumnNames() {
        if (columnNames == null)
            return(new String[0]);
        boolean[] isReal = realColumns();
        List<String> realList = new ArrayList<>();
        for (int i = 0; i < isReal.length; i++) {
            if (isReal[i]) {
                realList.add(columnNames.get(i));
            }
        }
        String[] realNames = new String[realList.size()];
        realNames = realList.toArray(realNames);
        return realNames;
    }

    public enum FieldType {
        /* Float or Double */
        Float {
            @Override public char getLetterCode() { return 'N'; }
            @Override int getFieldLengthCharBytes() { return 19; }
            @Override int getDecimalCount() { return 6; }
        },
        /* Any other number - Long, Integer, Short, Byte */
        Number {
            @Override public char getLetterCode() { return 'N'; }
            @Override int getFieldLengthCharBytes() { return 9; }
            @Override int getDecimalCount() { return 0; }
        },
        Boolean {
            @Override public char getLetterCode() { return 'C'; }
            @Override int getFieldLengthCharBytes() { return 6; }
            @Override int getDecimalCount() { return 0; }
        },
        String {
            @Override public char getLetterCode() { return 'C'; }
            @Override int getFieldLengthCharBytes() { return 254; }
            @Override int getDecimalCount() { return 0; }
        };

        /**
         * Returns the dbf letter code recognized by ArcMap.
         */
        public abstract char getLetterCode();
        /**
         * Returns the field length in one byte chars. ArcMap 9.3.1 uses
         * 19 for double precision and 9 for long.
         */
        abstract int getFieldLengthCharBytes();
        /**
         * Returns the decimal count.  This will be 0 except for floating point
         * numbers.
         *
         * In ArcMap (9.3.1) this sets the scale and the number format for a
         * double or float precision number.  The scale is the maximum number
         * places after the decimal that can be edited.
         */
        abstract int getDecimalCount();

        private static FieldType getBestFieldType(Object obj, FieldType existing) {
            FieldType type = FieldType.String;
            if (obj instanceof Double || obj instanceof Float) {
                type = FieldType.Float;
            } else if (obj instanceof Number) {
                type = FieldType.Number;
            } else if (obj instanceof Boolean) {
                type = FieldType.Boolean;
            }
            if (existing != null) {
                /* If you find a floating point then the best type is a floating
                 * point even if there are integer types also. */
                if (type == FieldType.Number && existing == FieldType.Float)
                    type = Float;

                boolean shouldBeString =
                        (type != existing) &&
                                !(type == FieldType.Float && existing == FieldType.Number);
                if (shouldBeString)
                    type = FieldType.String;
            }
            return type;
        }
    }

    public FieldType getColumnType(int columnIndex) {
        FieldType[] types = getColumnTypes();
        if (columnIndex < 0 || columnIndex >= types.length)
            return null;
        return types[columnIndex];
    }

    public FieldType[] getColumnTypes() {
        if (fieldTypeArray != null && fieldTypeArray.length == nFields)
            return fieldTypeArray;

        fieldTypeArray = new FieldType[nFields];
        for (int i = 0; i < data.length; i++) {
            Object[] record = data[i];
            for (int j = 0; j < fieldTypeArray.length; j++) {
                if (j >= record.length)
                    break;
                /* Skip null and empty records */
                if (record[j] == null || record[j].toString().trim().isEmpty())
                    continue;
                fieldTypeArray[j] = FieldType.getBestFieldType(
                        record[j],
                        fieldTypeArray[j]
                );
            }
        }

        for (int i = 0; i < fieldTypeArray.length; i++) {
            if (fieldTypeArray[i] == null)
                fieldTypeArray[i] = FieldType.String;
        }

        return fieldTypeArray;
    }

    private boolean[] realColumns() {
        boolean[] isReal = new boolean[nFields];
        Arrays.fill(isReal, false);
        checkAllRows:
        for (int row = 0; row < data.length; row++) {
            Object[] record = data[row];
            checkAllColumns:
            for (int col = 0; col < record.length; col++) {
                if (isReal[col])
                    continue checkAllColumns;
                /* Skip null and empty field values */
                if (record[col] == null || record[col].toString().trim().isEmpty())
                    continue checkAllColumns;
                String str = record[col].toString();
                try {
                    Double.parseDouble(str);
                    isReal[col] = true;
                    break checkAllColumns;
                } catch (NumberFormatException nfe) {
                }
            }
            boolean isAllTrue = true;
            for (int col = 0; col < record.length; col++) {
                isAllTrue = isAllTrue && isReal[col];
                if (!isAllTrue)
                    continue;
            }
            if (isAllTrue)
                break checkAllRows;
        }
        return isReal;
    }

    int getBestFieldLength(int col,  FieldType[] types) {
        if (col >= nFields || col < 0)
            return -1;
        if (col >= types.length)
            return -1;
        FieldType type = types[col];
        switch (type) {
            case Boolean:
            case Float:
            case Number:
                return type.getFieldLengthCharBytes();
            case String:
                int fieldLength = 1;
                for (int i = 0; i < data.length; i++) {
                    Object[] record = data[i];
                    if (col >= record.length)
                        continue;
                    Object obj = record[col];
                    if (obj instanceof String) {
                        int len = ((String) obj).length();
                        if (len > fieldLength)
                            fieldLength = len;
                    } else if (obj instanceof Float) {
                        if (FieldType.Float.getFieldLengthCharBytes() > fieldLength) {
                            fieldLength = FieldType.Float.getFieldLengthCharBytes();
                        }
                    } else if (obj instanceof Number) {
                        if (FieldType.Number.getFieldLengthCharBytes() > fieldLength) {
                            fieldLength = FieldType.Number.getFieldLengthCharBytes();
                        }
                    }
                }
                fieldLength = (5 - (fieldLength % 5)) + fieldLength;
                if (fieldLength > FieldType.String.getFieldLengthCharBytes())
                    fieldLength = FieldType.String.getFieldLengthCharBytes();
                return fieldLength;
        }

        return -1;
    }

    /**
     * @return data.
     */
    public Object[][] getData() {
        return data;
    }

    public Object[] getColumnData(String columnName) {
        if (columnName == null)
            return null;
        int index = -1;
        int nCols = columnNames.size();
        for (int i = 0; i < nCols; i++) {
            if (columnName.equals(columnNames.get(i))) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return null;

        int nValues = getNRecords();
        Object[] values = new Object[nValues];
        for (int row = 0; row < nValues; row++) {
            Object[] record = data[row];
            values[row] = record[index];
        }
        return values;
    }

    public double[] getRealData(String columnName) {
        if (columnName == null)
            return null;
        int index = -1;
        int nCols = columnNames.size();
        for (int i = 0; i < nCols; i++) {
            if (columnName.equals(columnNames.get(i))) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return null;

        int nValues = getNRecords();
        double[] values = new double[nValues];
        for (int row = 0; row < nValues; row++) {
            Object[] record = data[row];
            if (record[index] instanceof Number) {
                values[row] = ((Number) record[index]).doubleValue();
            } else if (record[index] != null) {
                String str = record[index].toString();
                try {
                    values[row] = Double.parseDouble(str);
                } catch (NumberFormatException nfe) {
                    values[row] = DVAL_DOUBLE;
                }
            } else {
                values[row] = DVAL_DOUBLE;
            }
        }
        return values;
    }

    public Object getValueAt(int row, int col) {
        if (data == null)
            return null;
        if (row < 0 || row >= data.length)
            return null;
        Object[] record = data[row];
        if (record == null)
            return null;
        if (col < 0 || col >= record.length)
            return null;
        Object value = record[col];
        String str = value.toString().trim();
        FieldType fieldType = getColumnType(col);
        if (fieldType != null) {
            switch (fieldType) {
                case Boolean:
                    if (str.equals("0"))
                        return Boolean.FALSE;
                    if (str.equals("1"))
                        return Boolean.TRUE;
                    return Boolean.valueOf(str);
                case Float:
                    try {
                        return NumberFormat.getInstance().parse(str).floatValue();
                    } catch (ParseException e) {
                        return 0.f;
                    }
                case Number:
                    try {
                        return NumberFormat.getInstance().parse(str).longValue();
                    } catch (ParseException e) {
                        return 0L;
                    }
                case String:
                    return str;
                default:
                    break;
            }
        }
        return value;
    }

    /**
     * Set the value at the specified row and column
     * @param value value
     * @param row row
     * @param col column
     * @return true if changed or false if not changed;
     */
    public boolean setValueAt(Object value, int row, int col) {
        if (data == null)
            return false;
        if (row < 0 || row >= data.length)
            return false;
        Object[] record = data[row];
        if (record == null)
            return false;
        if (col < 0 || col >= record.length)
            return false;
        if (record[col] == null && value == null)
            return false;
        if (record[col] == null || value == null) {
            record[col] = value;
            return true;
        }
        if (
                record[col] == value ||
                        record[col].equals(value) ||
                        value.toString().equals(record[col].toString())
        ) {
            return false;
        }
        record[col] = value;
        return true;
    }

    /**
     * @return number of fields (columns).
     */
    public int getNFields() {
        return nFields;
    }

    /**
     * @return number of records (rows).
     */
    public int getNRecords() {
        return nRecords;
    }

    public void dump(int indent) {
        StringBuffer stringBuffer = new StringBuffer(indent);
        for (int index = 0; index < indent; index++)
            stringBuffer.append(' ');
        String whiteSpace = stringBuffer.toString();
        System.out.println(whiteSpace+this.getClass().toString());
        whiteSpace += "  "; //$NON-NLS-1$

        System.out.print(whiteSpace);
        for (int field = 0; field < nFields; field++) {
            System.out.print(columnNames.get(field));
            if (field < nFields-1)
                System.out.print(",");
        }

        System.out.println();
        System.out.print(whiteSpace);

        for (int record = 0; record < nRecords; record++) {
            for (int field = 0; field < nFields; field++) {
                Object object = data[record][field];
                if (object != null) {
                    System.out.print(object.toString());
                    if (field < nFields-1)
                        System.out.print(",");
                }
            }
            System.out.println();
        }
    }
}
