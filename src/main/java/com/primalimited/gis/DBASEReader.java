package com.primalimited.gis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * dBASE (.dbf) file reader.
 *
 * @author Jim Newpower
 */
public class DBASEReader {
    private static final int DBASE_HEADER_LENGTH_BYTES = 32;
    public static final int FIELD_NAME_LENGTH = 11;

    // debug flags
    private static final boolean DEBUG_HEADER = false;
    private static final boolean DEBUG_COARSE = false;
    private static final boolean DEBUG_FINE = false;

    /**
     * Constructor
     *
     * @param inputStream .dbf input stream
     */
    public static boolean isDBFFile(InputStream inputStream) {
        DBASEReader reader = new DBASEReader();
        DBASEHeaderInfo headerInfo = null;
        try {
            headerInfo = reader.readHeader(inputStream);
        } catch (Exception ex) {
            return(false);
        }
        if (headerInfo == null)
            return(false);

        /* the number of records must be >= 0 */
        if (headerInfo.nRecords < 0)
            return(false);

        /* the .dbf file must have at least one field */
        if (headerInfo.nFields < 1)
            return(false);

        return(true);
    }

    /**
     * DBASE header info
     *
     * @author Jim Newpower
     */
    public static class DBASEHeaderInfo {
        private Calendar date = Calendar.getInstance();
        private byte description;
        private int nRecords = -1;
        private int nFields = -1;
        private short headerSize = -1;
        private short recordSize = -1;

        public void dump() {
            System.out.println("date:"+date.toString());
            System.out.println("description:"+description);
            System.out.println("nRecords:"+nRecords);
            System.out.println("headerSize:"+headerSize);
            System.out.println("recordSize:"+recordSize);
        }
        public Calendar getDate() {
            return date;
        }
        public void setDate(Calendar date) {
            this.date = date;
        }
        public int getNRecords() {
            return nRecords;
        }
        public void setNRecords(int records) {
            nRecords = records;
        }
        public final int getNFields() {
            return nFields;
        }
        public final void setNFields(int fields) {
            nFields = fields;
        }
        public short getHeaderSize() {
            return headerSize;
        }
        public void setHeaderSize(short headerSize) {
            this.headerSize = headerSize;
        }
        public short getRecordSize() {
            return recordSize;
        }
        public void setRecordSize(short recordSize) {
            this.recordSize = recordSize;
        }
        public byte getDescription() {
            return description;
        }
        public void setDescription(byte description) {
            this.description = description;
        }
    }

    /**
     * Reads header only from DBASE file
     *
     * @return new instance of DBASEHeaderInfo object
     * @throws IOException, ShapefileException
     */
    public DBASEHeaderInfo readHeader(InputStream inputStream) throws IOException, ShapefileException {
        byte[] data = new byte[DBASE_HEADER_LENGTH_BYTES];
        int nRead = inputStream.readNBytes(data, 0, DBASE_HEADER_LENGTH_BYTES);
        if (nRead != DBASE_HEADER_LENGTH_BYTES) {
            String message = String.format("Error: expected %d header bytes but read %d bytes.", DBASE_HEADER_LENGTH_BYTES, nRead);
            throw new ShapefileException(message);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        return readHeader(byteBuffer);
    }

    private DBASEHeaderInfo readHeader(ByteBuffer byteBuffer)  {
        DBASEHeaderInfo headerInfo = new DBASEHeaderInfo();

        /* make sure we have proper byte order */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /* make sure we are positioned at the start of the file */
        byteBuffer.rewind();

        // read description (byte 0)
        headerInfo.description = byteBuffer.get();
        if (debug()) {
            Byte byteValue = Byte.valueOf(headerInfo.description);
            System.out.println("description (byte):"+headerInfo.description);
            Integer integer = Integer.valueOf(byteValue.intValue());
            System.out.println("  Integer:"+integer);
            System.out.println("  Binary :"+Integer.toBinaryString(byteValue.intValue()));
            System.out.println("  Hex    :"+Integer.toHexString(byteValue.intValue()));
        }

        // read date bytes (byte 1-3)
        byte yearByte = byteBuffer.get();
        /*
         * The year stored in .dbf headers is the year since 1900, but it is only a
         * byte value (whose range is -128 to 127), therefore this file format is
         * really only valid until the year 2027.
         */
        int year = 1900 + yearByte;
        byte month = byteBuffer.get();
        byte day = byteBuffer.get();
        headerInfo.date.set(year, month - 1, day);
        if (debug()) {
            System.out.println("date bytes (y,m,d):"+yearByte+","+month+","+day);
            Timestamp ts = new Timestamp(headerInfo.date.getTimeInMillis());
            System.out.println("date:"+ts.toString());
        }

        // read n records (byte 4-7)
        byteBuffer.position(4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        headerInfo.nRecords = intBuffer.get();
        if (debug())
            System.out.println("nRecords:"+headerInfo.nRecords);

        // read header size (byte 8) and record size (byte 10)
        byteBuffer.position(8);
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        headerInfo.headerSize = shortBuffer.get();
        headerInfo.recordSize = shortBuffer.get();
        if (debug())
            System.out.println("headerSize:"+headerInfo.headerSize+",recordSize:"+headerInfo.recordSize);

        /* compute nFields from header size */
        headerInfo.nFields = (headerInfo.headerSize / 32) - 1;

        return(headerInfo);
    }

    private static boolean debug() {
        return (DEBUG_HEADER || DEBUG_FINE);
    }

    /*
     * Process a field for a record, returning an Object which represents
     * the data for that field (e.g. a String, Double, Integer, Boolean, etc.).
     */
    private Object processField(
            ByteBuffer byteBuffer,
            String fieldName,
            char fieldType,
            int fieldLength,
            int decimalCount
    ) {
        if (fieldLength < 0)
            return(null);

        int terminatorIndex = 0;
        switch (fieldType) {
            case 'C': // ASCII
                if (DEBUG_FINE)
                    System.out.print(fieldName+" ASCII Field ("+fieldLength+" chars):");
                char[] chars = new char[fieldLength];
                for (int index=0; index<fieldLength; index++) {
                    byte b = byteBuffer.get();
                    chars[index] = (char)b;
                }
                String name = new String(chars);
                if (DEBUG_FINE)
                    System.out.print(" padded:"+name);
                terminatorIndex = name.indexOf('\u0000');
                if (terminatorIndex > 0 && terminatorIndex < name.length())
                    name = name.substring(0, terminatorIndex);
                if (DEBUG_FINE)
                    System.out.println(" unpadded:"+name);
                return(name.trim());

            case 'D': // Date
                char[] dateChars = new char[fieldLength/*8*/];
                for (int index=0; index < fieldLength/*8*/; index++) {
                    byte b = byteBuffer.get();
                    dateChars[index] = (char)b;
                }
                String dateString = new String(dateChars);
                return(dateString);

            case 'N': // Numeric (integer)
                // do not assume that the creator of the file intended an integer here
            case 'F': // Numeric (floating point)
                if (DEBUG_FINE)
                    System.out.print(fieldName+" Numeric Field ("+fieldLength+" digits):");
                char[] fDigits = new char[fieldLength];
                for (int index=0; index<fieldLength; index++) {
                    byte b = byteBuffer.get();
                    char c = (char)b;
                    // replace unruly characters with 0's
                    if (c == ' ' || c == '\u0000')
                        fDigits[index] = ' ';
                    else
                        fDigits[index] = (char)b;
                }
                String fName = new String(fDigits).trim();
                if (DEBUG_FINE)
                    System.out.print("'"+fName+"'");
                Number numberObject = getNumberObject(fName);
                if (DEBUG_FINE)
                    System.out.println("="+numberObject + ", class: " + numberObject.getClass());

                return(numberObject);

            case 'L': // Logical (YyNnTtFf,space,?)
                byte b = byteBuffer.get();
                char c = (char)b;
                switch (c) {
                    case 'Y':
                    case 'y':
                    case 'T':
                    case 't':
                        return(Boolean.valueOf(true));
                    case 'N':
                    case 'n':
                    case 'F':
                    case 'f':
                    default:
                        return(Boolean.valueOf(false));
                }

            case 'M': // Memo--requires memo file: unsupported by shapefiles
                break;

            case 'V': // Variable--requires memo file: unsupported by shapefiles
                break;

            case 'P': // Picture--requires picture file: unsupported by shapefiles
                break;

            default:
                break;
        }
        return(null);
    }

    private Number getNumberObject(String value) {
        if (value == null) {
            return(null);
        }

        Number number = null;

        /* replace any + with spaces, then trim string of spaces */
        String cleanedStr = value.replace('+', ' ').trim();
        if (cleanedStr.isEmpty()) {
            return(null);
        }

        NumberFormat decimalFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        if (cleanedStr.toUpperCase().contains("E")) {
            /* if string contains '+', the parse() will blow up */
            int eIndex = cleanedStr.toUpperCase().indexOf('E');
            String mantissa = cleanedStr.substring(0, eIndex);

            Number mantissaNumber = null;
            try {
                mantissaNumber = decimalFormat.parse(mantissa.trim());
            } catch (ParseException ex) {
            }

            String exp = cleanedStr.substring(eIndex+1, cleanedStr.length());

            Number expNumber = null;
            try {
                expNumber = decimalFormat.parse(exp.trim());
            } catch (ParseException ex) {
            }

            if (mantissaNumber == null || expNumber == null) {
                return(null);
            }

            double val = mantissaNumber.doubleValue() * Math.pow(10.0, expNumber.intValue());
            return(Double.valueOf(val));
        }

        try {
            number = decimalFormat.parse(cleanedStr);

        } catch (ParseException e) {
            /* do nothing */
        }

        if (number == null)
            return(null);

        return(number);
    }

    /**
     * Get column names from the .dbf file; useful for displaying shapefile metadata.
     *
     * @param inputStream the .dbf input stream
     * @return array of column name strings
     * @throws IOException
     */
    public String[] readColumnNames(InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = createByteBuffer(inputStream);

        // process the file header
        DBASEHeaderInfo headerInfo = readHeader(byteBuffer);
        if (headerInfo.nRecords <= 0)
            return(null);

        if (debug())
            System.out.println("nRecords:"+headerInfo.nRecords);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (debug())
            System.out.println("nFields:"+nFields);
        if (nFields <= 0)
            return(null);
        String[] fieldNames = new String[nFields];
        for (int field = 0; field < nFields; field++) {
            /*
             * Read the Field Descriptor Array (starting at byte 32)
             */
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldNames[field] = fieldName;

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            charBuffer.get();

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            unsignedValue(byteBuffer.get());

            // decimal count
            byteBuffer.get();
        }

        return(fieldNames);
    }

    /**
     * Returns the unsigned byte value.
     *
     * @param b byte value
     * @return unsigned byte as an integer, to capture full byte range
     */
    static int unsignedValue(byte b) {
        return (b & 0xFF);
    }

    public Object[] readColumn(InputStream inputStream, String columnName) throws Exception {
        ByteBuffer byteBuffer = createByteBuffer(inputStream);

        // process the file header
        DBASEHeaderInfo headerInfo = readHeader(byteBuffer);
        if (headerInfo.nRecords <= 0)
            return(null);

        if (debug())
            System.out.println("nRecords:"+headerInfo.nRecords);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (debug())
            System.out.println("nFields:"+nFields);
        if (nFields <= 0)
            return(null);

        String[] fieldNames = new String[nFields];
        char[] fieldTypes = new char[nFields];
        int[] fieldLengths = new int[nFields];
        int[] decimalCounts = new int[nFields];
        int targetFieldIndex = -1;
        for (int field = 0; field < nFields; field++) {
            /*
             * Read the Field Descriptor Array (starting at byte 32)
             */
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            if (DEBUG_FINE)
                System.out.println("byteBuffer position:"+initialPosition);
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldName = fieldName.trim();
            fieldNames[field] = fieldName;
            if (columnName.compareTo(fieldName) == 0) {
                targetFieldIndex = field;
            }

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            char fieldType = charBuffer.get();
            fieldTypes[field] = fieldType;

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            int fieldLength = unsignedValue(byteBuffer.get());
            fieldLengths[field] = fieldLength;

            byte decimalCount = byteBuffer.get();
            decimalCounts[field] = decimalCount;
        }

        if (targetFieldIndex == -1)
            return(null);

        if (debug()) {
            System.out.print("Fields: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldNames[field]+",");
            System.out.println();

            System.out.print("Types: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldTypes[field]+",");
            System.out.println();

            System.out.print("Lengths: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldLengths[field]+",");
            System.out.println();

            System.out.print("Decimal Counts: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(decimalCounts[field]+",");
            System.out.println();
        }

        if (DEBUG_COARSE)
            System.out.println("targetColumnIndex="+targetFieldIndex);

        // read the Header Record Terminator byte
        byteBuffer.get();

        /*
         * Read records
         */
        Object[] data = new Object[headerInfo.nRecords];
        for (int record = 0; record < headerInfo.nRecords; record++) {
            // set byteBuffer position
            int initialPosition = headerInfo.headerSize + (record * headerInfo.recordSize);
            if (DEBUG_FINE)
                System.out.println("record "+record+" byteBuffer position:"+initialPosition);
            byteBuffer.position(initialPosition);

            // read deleted byte for this record
            /* byte deletedByte = */ byteBuffer.get();

            int position = byteBuffer.position();
            for (int field = 1; field <= targetFieldIndex; field++) {
                if (field > 0)
                    position += fieldLengths[field-1];
            }
            byteBuffer.position(position);
            if (DEBUG_FINE)
                System.out.println("record:"+record+ " position:"+position);
            data[record] = processField(
                    byteBuffer,
                    fieldNames[targetFieldIndex],
                    fieldTypes[targetFieldIndex],
                    fieldLengths[targetFieldIndex],
                    decimalCounts[targetFieldIndex]
            );
        }

        return(data);
    }

    public Class<?> readColumnType(InputStream inputStream, String columnName) throws Exception {
        if (DEBUG_COARSE)
            System.out.println("readColumn() columnName="+columnName);

        // process the file header
        inputStream.mark(DBASE_HEADER_LENGTH_BYTES * 100);
        DBASEHeaderInfo headerInfo = readHeader(inputStream);
        if (headerInfo.nRecords <= 0)
            return(null);
        inputStream.reset();

        // Allocate byte buffer to read column names
        byte[] data = inputStream.readAllBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        /* make sure we have proper byte order */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /* make sure we are positioned at the start of the file */
        byteBuffer.rewind();

        /* make sure we have proper byte order */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /* make sure we are positioned at the start of the file */
        byteBuffer.rewind();

        if (debug())
            System.out.println("nRecords:"+headerInfo.nRecords);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (debug())
            System.out.println("nFields:"+nFields);
        if (nFields <= 0)
            return(null);
        String[] fieldNames = new String[nFields];
        char[] fieldTypes = new char[nFields];
        int[] fieldLengths = new int[nFields];
        int[] decimalCounts = new int[nFields];
        int targetFieldIndex = -1;
        for (int field = 0; field < nFields; field++) {
            /*
             * Read the Field Descriptor Array (starting at byte 32)
             */
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            if (DEBUG_FINE)
                System.out.println("byteBuffer position:"+initialPosition);
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldName = fieldName.trim();
            fieldNames[field] = fieldName;
            if (columnName.compareTo(fieldName) == 0) {
                targetFieldIndex = field;
            }

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            char fieldType = charBuffer.get();
            fieldTypes[field] = fieldType;

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            int fieldLength = unsignedValue(byteBuffer.get());
            fieldLengths[field] = fieldLength;

            byte decimalCount = byteBuffer.get();
            decimalCounts[field] = decimalCount;
        }

        if (targetFieldIndex == -1)
            return(null);

        char fieldType = fieldTypes[targetFieldIndex];
        switch (fieldType) {
            case 'C': // ASCII
                return String.class;
            case 'D': // Date
                return Date.class;
            case 'N': // Numeric (integer) (do not assume that the creator of the file intended an integer here)
            case 'F': // Numeric (floating point)
                return Double.class;
            case 'L': // Logical (YyNnTtFf,space,?)
                return Boolean.class;
            case 'M': // Memo--requires memo file: unsupported by shapefiles
                break;

            case 'V': // Variable--requires memo file: unsupported by shapefiles
                break;

            case 'P': // Picture--requires picture file: unsupported by shapefiles
                break;

            default:
                break;
        }

        return String.class;
    }

    /**
     * Read a record from the .dbf file; useful for e.g. user clicks on shape feature and wants to see the
     * metadata associated with that record.
     *
     * @param stream the input stream.
     * @param headerInfo the .dbf header info.
     * @param record the record index.
     * @return list of db fields.
     * @throws IOException
     */
    public List<DBField> readRecord(InputStream stream, DBASEHeaderInfo headerInfo, int record) throws IOException {
        ByteBuffer byteBuffer = createByteBuffer(stream);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (nFields <= 0)
            return Collections.emptyList();

        String[] fieldNames = new String[nFields];
        char[] fieldTypes = new char[nFields];
        int[] fieldLengths = new int[nFields];
        int[] decimalCounts = new int[nFields];
        for (int field = 0; field < nFields; field++) {
            // Read the Field Descriptor Array (starting at byte 32)
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldNames[field] = fieldName;

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            char fieldType = charBuffer.get();
            fieldTypes[field] = fieldType;

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            int fieldLength = unsignedValue(byteBuffer.get());
            fieldLengths[field] = fieldLength;

            byte decimalCount = byteBuffer.get();
            decimalCounts[field] = decimalCount;
        }

        int nBytesToSkip = nFields * DBASE_HEADER_LENGTH_BYTES;

        // read the Header Record Terminator byte
        nBytesToSkip++;

        nBytesToSkip += headerInfo.headerSize + (record * headerInfo.recordSize);

        byteBuffer.position(nBytesToSkip);

        // set byteBuffer position
        int initialPosition = headerInfo.headerSize + (record * headerInfo.recordSize);
        if (DEBUG_FINE)
            System.out.println("record "+record+" byteBuffer position:"+initialPosition);
        byteBuffer.position(initialPosition);

        // read deleted byte for this record
        byteBuffer.get();

        List<DBField> dbFields = new ArrayList<>(nFields);

        int position = byteBuffer.position();
        for (int field = 0; field < nFields; field++) {
            if (field > 0)
                position += fieldLengths[field-1];
            byteBuffer.position(position);
            if (DEBUG_FINE)
                System.out.println("record:"+record+ " field:" + field + " position:"+position);
            Object value = processField(
                    byteBuffer,
                    fieldNames[field],
                    fieldTypes[field],
                    fieldLengths[field],
                    decimalCounts[field]
            );

            dbFields.add(new DBField(fieldNames[field], String.valueOf(fieldTypes[field]), value));
        }

        return Collections.unmodifiableList(dbFields);
    }

    private ByteBuffer createByteBuffer(InputStream stream) throws IOException {
        // Allocate byte buffer to read column names
        byte[] bytes = stream.readAllBytes();
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        /* make sure we have proper byte order */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /* make sure we are positioned at the start of the file */
        byteBuffer.rewind();

        return byteBuffer;
    }

    public DBASETableData read(InputStream inputStream) throws IOException {
        ByteBuffer byteBuffer = createByteBuffer(inputStream);

        // process the file header
        DBASEHeaderInfo headerInfo = readHeader(byteBuffer);
        if (headerInfo.nRecords <= 0)
            return(null);

        if (debug())
            System.out.println("nRecords:"+headerInfo.nRecords);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (debug())
            System.out.println("nFields:"+nFields);
        if (nFields <= 0)
            return(null);
        String[] fieldNames = new String[nFields];
        char[] fieldTypes = new char[nFields];
        int[] fieldLengths = new int[nFields];
        int[] decimalCounts = new int[nFields];
        for (int field = 0; field < nFields; field++) {
            /*
             * Read the Field Descriptor Array (starting at byte 32)
             */
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldNames[field] = fieldName;

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            char fieldType = charBuffer.get();
            fieldTypes[field] = fieldType;

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            int fieldLength = unsignedValue(byteBuffer.get());
            fieldLengths[field] = fieldLength;

            byte decimalCount = byteBuffer.get();
            decimalCounts[field] = decimalCount;
        }

        if (debug()) {
            System.out.print("Fields: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldNames[field]+",");
            System.out.println();

            System.out.print("Types: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldTypes[field]+",");
            System.out.println();

            System.out.print("Lengths: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(fieldLengths[field]+",");
            System.out.println();

            System.out.print("Decimal Counts: ");
            for (int field = 0; field < nFields; field++)
                System.out.print(decimalCounts[field]+",");
            System.out.println();
        }

        // read the Header Record Terminator byte
        byteBuffer.get();

        /*
         * Read records
         */
        Object[][] data = new Object[headerInfo.nRecords][nFields];
        for (int record = 0; record < headerInfo.nRecords; record++) {
            // set byteBuffer position
            int initialPosition = headerInfo.headerSize + (record * headerInfo.recordSize);
            if (DEBUG_FINE)
                System.out.println("record "+record+" byteBuffer position:"+initialPosition);
            byteBuffer.position(initialPosition);

            // read deleted byte for this record
            /* byte deletedByte = */ byteBuffer.get();

            int position = byteBuffer.position();
            for (int field = 0; field < nFields; field++) {
                if (field > 0)
                    position += fieldLengths[field-1];
                byteBuffer.position(position);
                if (DEBUG_FINE)
                    System.out.println("record:"+record+ " field:" + field + " position:"+position);
                data[record][field] = processField(
                        byteBuffer,
                        fieldNames[field],
                        fieldTypes[field],
                        fieldLengths[field],
                        decimalCounts[field]
                );
            }
        }

        DBASETableData returnData = new DBASETableData(
                data,
                headerInfo.nRecords,
                nFields,
                fieldNames
        );
        if (debug())
            returnData.dump(0/*indent*/);

        return(returnData);
    }

    public DBASETableData readWithRecordNumberRowHeaders(ByteBuffer byteBuffer) {
        /* make sure we have proper byte order */
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        /* make sure we are positioned at the start of the file */
        byteBuffer.rewind();

        // process the file header
        DBASEHeaderInfo headerInfo = readHeader(byteBuffer);
        if (headerInfo.nRecords <= 0)
            return(null);

        int nFields = (headerInfo.headerSize / DBASE_HEADER_LENGTH_BYTES) - 1;
        if (nFields <= 0)
            return(null);
        String[] fieldNames = new String[nFields + 1];
        fieldNames[0] = "Record No.";
        char[] fieldTypes = new char[nFields];
        int[] fieldLengths = new int[nFields];
        int[] decimalCounts = new int[nFields];
        for (int field = 0; field < nFields; field++) {
            /* Read the Field Descriptor Array (starting at byte 32) */
            int initialPosition = (field+1) * DBASE_HEADER_LENGTH_BYTES;
            byteBuffer.position(initialPosition);
            // field name (11 bytes)--convert bytes to unicode chars
            char[] uniChars = new char[FIELD_NAME_LENGTH];
            for (int index=0; index<FIELD_NAME_LENGTH; index++) {
                byte charByte = byteBuffer.get();
                char uniChar = (char)charByte;
                uniChars[index] = uniChar;
            }
            String fieldName = new String(uniChars);
            int terminatorIndex = fieldName.indexOf('\u0000');
            if (terminatorIndex > 0 && terminatorIndex < fieldName.length())
                fieldName = fieldName.substring(0, terminatorIndex);
            fieldNames[field + 1] = fieldName;

            // field type
            CharBuffer charBuffer = byteBuffer.asCharBuffer();
            char fieldType = charBuffer.get();
            fieldTypes[field] = fieldType;

            // position buffer to 16th byte in record (field length)
            byteBuffer.position(initialPosition + 16);
            int fieldLength = unsignedValue(byteBuffer.get());
            fieldLengths[field] = fieldLength;

            byte decimalCount = byteBuffer.get();
            decimalCounts[field] = decimalCount;
        }
        // read the Header Record Terminator byte
        byteBuffer.get();
        Object[][] data = new Object[headerInfo.nRecords][nFields + 1/*row header*/];
        for (int record = 0; record < headerInfo.nRecords; record++) {
            // set byteBuffer position
            int initialPosition = headerInfo.headerSize + (record * headerInfo.recordSize);
            if (DEBUG_FINE)
                System.out.println("record "+record+" byteBuffer position:"+initialPosition);
            byteBuffer.position(initialPosition);

            // read deleted byte for this record
            byteBuffer.get();
            data[record][0] = Integer.valueOf(record + 1);
            int position = byteBuffer.position();
            for (int field = 0; field < nFields; field++) {
                if (field > 0)
                    position += fieldLengths[field-1];
                byteBuffer.position(position);
                if (DEBUG_FINE)
                    System.out.println("record:"+record+ " field:" + field + " position:"+position);
                data[record][field+1] = processField(
                        byteBuffer,
                        fieldNames[field],
                        fieldTypes[field],
                        fieldLengths[field],
                        decimalCounts[field]
                );
            }
        }
        DBASETableData returnData = new DBASETableData(
                data,
                headerInfo.nRecords,
                nFields + 1,
                fieldNames
        );
        return(returnData);
    }

  /*
  dBASE .DBF File Structure - by Borland Developer Support Staff

  Technical Information Database

  TI838D.txt   dBASE .DBF File Structure
  Category   :Database Programming
  Platform    :All
  Product    :Delphi  All

  Description:
  Sometimes it is necessary to delve into a dBASE table outside the control
  of the Borland Database Engine (BDE). For instance, if the .DBT file (that
  contains memo data) for a given table is irretrievably lost, the file will
  not be usable because the byte in the file header indicates that there
  should be a corresponding memo file. This necessitates toggling this byte
  to indicate no such accompanying memo file. Or, you may just want to write
  your own data access routine.

  Below are the file structures for dBASE table files. Represented are the
  file structures as used for various versions of dBASE: dBASE III PLUS 1.1,
  dBASE IV 2.0, dBASE 5.0 for DOS, and dBASE 5.0 for Windows.

  **************************************************************************
  The data file header structure for dBASE III PLUS table file.
  **************************************************************************

  The table file header:
  ======================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0     1 byte      Valid dBASE III PLUS table file (03h without a memo
                    (.DBT file; 83h with a memo).

  1-3   3 bytes     Date of last update; in YYMMDD format.
  4-7   32-bit      Number of records in the table.
        number
  8-9   16-bit      Number of bytes in the header.
        number
  10-11 16-bit      Number of bytes in the record.
        number
  12-14 3 bytes     Reserved bytes.
  15-27 13 bytes    Reserved for dBASE III PLUS on a LAN.
  28-31 4 bytes     Reserved bytes.
  32-n  32 bytes    Field descriptor array (the structure of this array is
        each        shown below)
  n+1   1 byte      0Dh stored as the field terminator.

  n above is the last byte in the field descriptor array. The size of the
  array depends on the number of fields in the table file.

  Table Field Descriptor Bytes
  ============================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0-10  11 bytes    Field name in ASCII (zero-filled).
  11    1 byte      Field type in ASCII (C, D, L, M, or N).
  12-15 4 bytes     Field data address (address is set in memory; not useful
                    on disk).
  16    1 byte      Field length in binary.
  17    1 byte      Field decimal count in binary.
  18-19 2 bytes     Reserved for dBASE III PLUS on a LAN.
  20    1 byte      Work area ID.
  21-22 2 bytes     Reserved for dBASE III PLUS on a LAN.
  23    1 byte      SET FIELDS flag.
  24-31 1 byte      Reserved bytes.

  Table Records
  =============

  The records follow the header in the table file. Data records are preceded
  by one byte, that is, a space (20h) if the record is not deleted, an
  asterisk (2Ah) if the record is deleted. Fields are packed into records
  without field separators orrecord terminators. The end of the file is
  marked by a single byte, with the end-of-file marker, an OEM code page
  character value of 26 (1Ah). You can input OEM code page data as indicated
  below.

  Allowable Input for dBASE Data Types
  ====================================

  Data Type      Data Input
  -------------- -----------------------------------------------------------
  C (Character)  All OEM code page characters.
  D (Date)       Numbers and a character to separate month, day, and year
                 (stored internally as 8 digits in YYYYMMDD format).
  N (Numeric)    - . 0 1 2 3 4 5 6 7 8 9
  L (Logical)    ? Y y N n T t F f (? when not initialized).
  M (Memo)       All OEM code page characters (stored internally as 10
                 digits representing a .DBT block number).

  Binary, Memo, and OLE Fields And .DBT Files
  ===========================================

  Memo fields store data in .DBT files consisting of blocks numbered
  sequentially (0, 1, 2, and so on). The size of these blocks are internally
  set to 512 bytes. The first block in the .DBT file, block 0, is the .DBT
  file header.

  Memo field of each record in the .DBF file contains the number of the
  block (in OEM code page values) where the field's data actually begins. If
  a field contains no data, the .DBF file contains blanks (20h) rather than
  a number.

  When data is changed in a field, the block numbers may also change and the
  number in the .DBF may be changed to reflect the new location.

  This information is from the Using dBASE III PLUS manual, Appendix C.

  **************************************************************************
  The data file header structure for dBASE IV 2.0 table file.
  **************************************************************************

  File Structure:
  ===============

  Byte     Contents       Meaning
  -------  ----------     -------------------------------------------------
  0        1byte          Valid dBASE IV file; bits 0-2 indicate version
                          number, bit 3 the presence of a dBASE IV memo
                          file, bits 4-6 the presence of an SQL table, bit
                          7 the presence of any memo file (either dBASE III
                          PLUS or dBASE IV).
  1-3      3 bytes        Date of last update; formattted as YYMMDD.
  4-7      32-bit number  Number of records in the file.
  8-9      16-bit number  Number of bytes in the header.
  10-11    16-bit number  Number of bytes in the record.
  12-13    2 bytes        Reserved; fill with 0.
  14       1 byte         Flag indicating incomplete transaction.
  15       1 byte         Encryption flag.
  16-27    12 bytes       Reserved for dBASE IV in a multi-user environment.
  28       1 bytes        Production MDX file flag; 01H if there is an MDX,
                          00H if not.
  29       1 byte         Language driver ID.
  30-31    2 bytes        Reserved; fill with 0.
  32-n*    32 bytes each  Field descriptor array (see below).
  n + 1    1 byte         0DH as the field terminator.

  * n is the last byte in the field descriptor array. The size of the array
  depends on the number of fields in the database file.

  The field descriptor array:
  ===========================

  Byte     Contents       Meaning
  -------  ------------   --------------------------------------------------
  0-10     11 bytes       Field name in ASCII (zero-filled).
  11       1 byte         Field type in ASCII (C, D, F, L, M, or N).
  12-15    4 bytes        Reserved.
  16       1 byte         Field length in binary.
  17       1 byte         Field decimal count in binary.
  18-19    2 bytes        Reserved.
  20       1 byte         Work area ID.
  21-30    10 bytes       Reserved.
  31       1 byte         Production MDX field flag; 01H if field has an
                          index tag in the production MDX file, 00H if not.

  Database records:
  =================

  The records follow the header in the database file. Data records are
  preceded by one byte; that is, a space (20H) if the record is not deleted,
  an asterisk (2AH) if the record is deleted. Fields are packed into
  records without field separators or record terminators. The end of the
  file is marked by a single byte, with the end-of-file marker an ASCII 26
  (1AH) character.

  Allowable Input for dBASE Data Types:
  ====================================

  Data  Type           Data Input
  ----  ----------     -----------------------------------------------------
  C     (Character)    All OEM code page characters.
  D     (Date)         Numbers and a character to separate month, day, and
                       year (stored internally as 8 digits in YYYYMMDD
                       format).
  F     (Floating      - . 0 1 2 3 4 5 6 7 8 9
        point binary
        numeric)
  N     (Binary        - . 0 1 2 3 4 5 6 7 8 9
        coded decimal
        numeric)
  L     (Logical)      ? Y y N n T t F f (? when not initialized).
  M     (Memo)         All OEM code page characters (stored internally as 10
                       digits representing a .DBT block number).

  Memo Fields And .DBT Files
  ===========================================

  Memo fields store data in .DBT files consisting of blocks numbered
  sequentially (0, 1, 2, and so on). SET BLOCKSIZE determines the size of
  each block. The first block in the .DBT file, block 0, is the .DBT file
  header.

  Each memo field of each record in the .DBF file contains the number of the
  block (in OEM code page values) where the field's data actually begins. If
  a field contains no data, the .DBF file contains blanks (20h) rather than
  a number.

  When data is changed in a field, the block numbers may also change and the
  number in the .DBF may be changed to reflect the new location.

  This information is from the dBASE IV Language Reference manual, Appendix
  D.

  **************************************************************************
  The data file header structure for dBASE 5.0 for DOS table file.
  **************************************************************************

  The table file header:
  ======================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0     1 byte      Valid dBASE for Windows table file; bits 0-2 indicate
                    version number; bit 3 indicates presence of a dBASE IV
                    or dBASE for Windows memo file; bits 4-6 indicate the
                    presence of a dBASE IV SQL table; bit 7 indicates the
                    presence of any .DBT memo file (either a dBASE III PLUS
                    type or a dBASE IV or dBASE for Windows memo file).
  1-3   3 bytes     Date of last update; in YYMMDD format.
  4-7   32-bit      Number of records in the table.
        number
  8-9   16-bit      Number of bytes in the header.
        number
  10-11 16-bit      Number of bytes in the record.
        number
  12-13 2 bytes     Reserved; filled with zeros.
  14    1 byte      Flag indicating incomplete dBASE transaction.
  15    1 byte      Encryption flag.
  16-27 12 bytes    Reserved for multi-user processing.
  28    1 byte      Production MDX flag; 01h stored in this byte if a prod-
                    uction .MDX file exists for this table; 00h if no .MDX
                    file exists.
  29    1 byte      Language driver ID.
  30-31 2 bytes     Reserved; filled with zeros.
  32-n  32 bytes    Field descriptor array (the structure of this array is
        each        shown below)
  n+1   1 byte      0Dh stored as the field terminator.

  n above is the last byte in the field descriptor array. The size of the
  array depends on the number of fields in the table file.

  Table Field Descriptor Bytes
  ============================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0-10  11 bytes    Field name in ASCII (zero-filled).
  11    1 byte      Field type in ASCII (B, C, D, F, G, L, M, or N).
  12-15 4 bytes     Reserved.
  16    1 byte      Field length in binary.
  17    1 byte      Field decimal count in binary.
  18-19 2 bytes     Reserved.
  20    1 byte      Work area ID.
  21-30 10 bytes    Reserved.
  31    1 byte      Production .MDX field flag; 01h if field has an index
                    tag in the production .MDX file; 00h if the field is not
                    indexed.

  Table Records
  =============

  The records follow the header in the table file. Data records are preceded
  by one byte, that is, a space (20h) if the record is not deleted, an
  asterisk (2Ah) if the record is deleted. Fields are packed into records
  without field separators orrecord terminators. The end of the file is
  marked by a single byte, with the end-of-file marker, an OEM code page
  character value of 26 (1Ah). You can input OEM code page data as indicated
  below.

  Allowable Input for dBASE Data Types
  ====================================

  Data Type      Data Input
  -------------- -----------------------------------------------------------
  C (Character)  All OEM code page characters.
  D (Date)       Numbers and a character to separate month, day, and year
                 (stored internally as 8 digits in YYYYMMDD format).
  F (Floating    - . 0 1 2 3 4 5 6 7 8 9
    point binary
    numeric)
  N (Numeric)    - . 0 1 2 3 4 5 6 7 8 9
  L (Logical)    ? Y y N n T t F f (? when not initialized).
  M (Memo)       All OEM code page characters (stored internally as 10
                 digits representing a .DBT block number).

  Memo Fields And .DBT Files
  ===========================================

  Memo fields store data in .DBT files consisting of blocks numbered
  sequentially (0, 1, 2, and so on). SET BLOCKSIZE determines the size of
  each block. The first block in the .DBT file, block 0, is the .DBT file
  header.

  Each memo field of each record in the .DBF file contains the number of the
  block (in OEM code page values) where the field's data actually begins. If
  a field contains no data, the .DBF file contains blanks (20h) rather than
  a number.

  When data is changed in a field, the block numbers may also change and the
  number in the .DBF may be changed to reflect the new location.

  Unlike dBASE III PLUS, if you delete text in a memo field, dBASE 5.0 for
  DOS may reuse the space from the deleted text when you input new text.
  dBASE III PLUS always appends new text to the end of the .DBT file. In
  dBASE III PLUS, the .DBT file size grows whenever new text is added, even
  if other text in the file is deleted.

  This information is from the dBASE for DOS Language Reference manual,
  Appendix C.

  **************************************************************************
  The data file header structure for dBASE 5.0 for Windows table file.
  **************************************************************************

  The table file header:
  ======================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0     1 byte      Valid dBASE for Windows table file; bits 0-2 indicate
                    version number; bit 3 indicates presence of a dBASE IV
                    or dBASE for Windows memo file; bits 4-6 indicate the
                    presence of a dBASE IV SQL table; bit 7 indicates the
                    presence of any .DBT memo file (either a dBASE III PLUS
                    type or a dBASE IV or dBASE for Windows memo file).
  1-3   3 bytes     Date of last update; in YYMMDD format.
  4-7   32-bit      Number of records in the table.
        number
  8-9   16-bit      Number of bytes in the header.
        number
  10-11 16-bit      Number of bytes in the record.
        number
  12-13 2 bytes     Reserved; filled with zeros.
  14    1 byte      Flag indicating incomplete dBASE IV transaction.
  15    1 byte      dBASE IV encryption flag.
  16-27 12 bytes    Reserved for multi-user processing.
  28    1 byte      Production MDX flag; 01h stored in this byte if a prod-
                    uction .MDX file exists for this table; 00h if no .MDX
                    file exists.
  29    1 byte      Language driver ID.
  30-31 2 bytes     Reserved; filled with zeros.
  32-n  32 bytes    Field descriptor array (the structure of this array is
        each        shown below)
  n+1   1 byte      0Dh stored as the field terminator.

  n above is the last byte in the field descriptor array. The size of the
  array depends on the number of fields in the table file.

  Table Field Descriptor Bytes
  ============================

  Byte  Contents    Description
  ----- --------    --------------------------------------------------------
  0-10  11 bytes    Field name in ASCII (zero-filled).
  11    1 byte      Field type in ASCII (B, C, D, F, G, L, M, or N).
  12-15 4 bytes     Reserved.
  16    1 byte      Field length in binary.
  17    1 byte      Field decimal count in binary.
  18-19 2 bytes     Reserved.
  20    1 byte      Work area ID.
  21-30 10 bytes    Reserved.
  31    1 byte      Production .MDX field flag; 01h if field has an index
                    tag in the production .MDX file; 00h if the field is not
                    indexed.

  Table Records
  =============

  The records follow the header in the table file. Data records are preceded
  by one byte, that is, a space (20h) if the record is not deleted, an
  asterisk (2Ah) if the record is deleted. Fields are packed into records
  without field separators orrecord terminators. The end of the file is
  marked by a single byte, with the end-of-file marker, an OEM code page
  character value of 26 (1Ah). You can input OEM code page data as indicated
  below.

  Allowable Input for dBASE Data Types
  ====================================

  Data Type      Data Input
  -------------- -----------------------------------------------------------
  B (Binary)     All OEM code page characters (stored internally as 10
                 digits representing a .DBT block number).
  C (Character)  All OEM code page characters.
  D (Date)       Numbers and a character to separate month, day, and year
                 (stored internally as 8 digits in YYYYMMDD format).
  G (General     All OEM code page characters (stored internally as 10
                 digits or OLE) representing a .DBT block number).
  N (Numeric)    - . 0 1 2 3 4 5 6 7 8 9
  L (Logical)    ? Y y N n T t F f (? when not initialized).
  M (Memo)       All OEM code page characters (stored internally as 10
                 digits representing a .DBT block number).

  Binary, Memo, and OLE Fields And .DBT Files
  ===========================================

  Binary, memo, and OLE fields store data in .DBT files consisting of blocks
  numbered sequentially (0, 1, 2, and so on). SET BLOCKSIZE determines the
  size of each block. The first block in the .DBT file, block 0, is the .DBT
  file header.

  Each binary, memo, or OLE field of each record in the .DBF file contains
  the number of the block (in OEM code page values) where the field's data
  actually begins. If a field contains no data, the .DBF file contains
  blanks (20h) rather than a number.

  When data is changed in a field, the block numbers may also change and the
  number in the .DBF may be changed to reflect the new location.

  Unlike dBASE III PLUS, if you delete text in a memo field (or binary and
  OLE fields), dBASE for Windows (unlike dBASE IV) may reuse the space from
  the deleted text when you input new text. dBASE III PLUS always appends
  new text to the end of the .DBT file. In dBASE III PLUS, the .DBT file
  size grows whenever new text is added, even if other text in the file is
  deleted.

  This information is from the dBASE for Windows Language Reference manual,
  Appendix C.

  Reference:
  7/16/98 4:33:55 PM
  */
}
