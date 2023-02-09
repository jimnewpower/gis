package com.primalimited.gis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DBASEReaderTest {

    @Test
    public void readerTest() throws IOException {
        TestHelper testHelper = new TestHelper();

        DBASEReader reader = new DBASEReader();

        InputStream is = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        DBASETableData data = reader.read(is);
        is.close();

        assertEquals(3072, data.getNRecords());
        assertEquals(8, data.getNFields());

        DBASETableData.FieldType[] expectedFieldTypes = new DBASETableData.FieldType[]{
                DBASETableData.FieldType.String,
                DBASETableData.FieldType.Number,
                DBASETableData.FieldType.String,
                DBASETableData.FieldType.String,
                DBASETableData.FieldType.String,
                DBASETableData.FieldType.String,
                DBASETableData.FieldType.Float,
                DBASETableData.FieldType.Number
        };
        DBASETableData.FieldType[] fieldTypes = data.getColumnTypes();
        for (int i = 0; i < data.getNFields(); i++)
            assertEquals(expectedFieldTypes[i], fieldTypes[i]);
    }

    @Test
    void readSingleRecordTest() throws IOException, ShapefileException {
        final int record = 4;
        final String[] expected = new String[] {
                "DBRecord{name='tnmid', type='C', value={E04A85C8-DAF0-4927-97E8-B2A581988AD6}}",
                "DBRecord{name='hudigit', type='N', value=2}",
                "DBRecord{name='humod', type='C', value=NM}",
                "DBRecord{name='linesource', type='C', value=DRG24}",
                "DBRecord{name='metasource', type='C', value={596E21CA-D6C3-4BEC-9C18-BF63FB3727D4}}",
                "DBRecord{name='loaddate', type='D', value=20170918}",
                "DBRecord{name='shape_Leng', type='N', value=0.013047043209515}",
                "DBRecord{name='ObjectID', type='N', value=5}"
        };

        TestHelper testHelper = new TestHelper();
        DBASEReader reader = new DBASEReader();
        InputStream is = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);

        List<DBField> fields = reader.readRecord(is, record);
        is.close();

        assertEquals(8, fields.size(), "number of fields");
        for (int i = 0; i < fields.size(); i++) {
            assertEquals(expected[i], fields.get(i).toString(), "field " + i + " toString()");
        }
    }

    @Test
    void readColumnNames() throws ShapefileException, IOException {
        TestHelper testHelper = new TestHelper();

        DBASEReader reader = new DBASEReader();
        InputStream is = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        String[] columnNames = reader.readColumnNames(is);

        final String[] expected = new String[] {
                "tnmid",
                "hudigit",
                "humod",
                "linesource",
                "metasource",
                "loaddate",
                "shape_Leng",
                "ObjectID"
        };

        assertArrayEquals(expected, columnNames, "column names from .dbf file");
    }
}