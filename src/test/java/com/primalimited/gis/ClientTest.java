package com.primalimited.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientTest {

    @Test
    void clientTest_WBDLine() throws Exception {
        TestHelper testHelper = new TestHelper();

        // Get the input stream.
        InputStream mainInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainInputStream);

        // Read the entire geometry collection from the shapefile.
        GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
        mainInputStream.close();

        // Mimic use case: User clicks on a polyline on the map, returning the unique index into the geometry collection.
        final int selectedRecordIndex = 4;

        // Select the specific geometry from the record index.
        Geometry geometry = geometryCollection.getGeometryN(selectedRecordIndex);
        UserData userData = new UserData(geometry.getUserData());
        assertEquals(selectedRecordIndex, userData.toInt(), "user data set during read should be the record index");
        assertEquals(
                "LINESTRING (-105.672586 38.140343, -105.672516 38.140245, -105.672527 38.140154, -105.672583 38.140001, -105.672762 38.139648, -105.673032 38.139241, -105.673156 38.139015, -105.673242 38.138483, -105.673302 38.137599, -105.673254 38.137266, -105.673147 38.136789, -105.672982 38.136249, -105.672807 38.135854, -105.672634 38.135629, -105.672541 38.135396, -105.672493 38.135225, -105.6724 38.134955, -105.672305 38.134595, -105.672211 38.134325, -105.671969 38.134002, -105.671691 38.133589, -105.671053 38.132412, -105.670832 38.132026, -105.670324 38.131325, -105.670232 38.1312, -105.670094 38.13112, -105.669945 38.131048, -105.669807 38.130968, -105.669715 38.130856, -105.669691 38.13077, -105.669725 38.130689, -105.669781 38.130563, -105.669927 38.130327, -105.669972 38.130237, -105.66997 38.13003, -105.669943 38.129597, -105.669907 38.129408, -105.669917 38.129264, -105.669984 38.129092, -105.670055 38.128661)",
                geometry.toString()
        );

        // Find record in dbase file, based on record index from user data.
        DBASEReader reader = new DBASEReader();
        InputStream dbfStream = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        List<DBField> fields = reader.readRecord(dbfStream, userData.toInt());
        dbfStream.close();
        assertEquals(
                "[DBRecord{name='tnmid', type='C', value={E04A85C8-DAF0-4927-97E8-B2A581988AD6}}, DBRecord{name='hudigit', type='N', value=2}, DBRecord{name='humod', type='C', value=NM}, DBRecord{name='linesource', type='C', value=DRG24}, DBRecord{name='metasource', type='C', value={596E21CA-D6C3-4BEC-9C18-BF63FB3727D4}}, DBRecord{name='loaddate', type='D', value=20170918}, DBRecord{name='shape_Leng', type='N', value=0.013047043209515}, DBRecord{name='ObjectID', type='N', value=5}]",
                Arrays.toString(fields.toArray())
        );

        // Convert line string to geoJSON string.
        org.locationtech.jts.io.geojson.GeoJsonWriter writer = new org.locationtech.jts.io.geojson.GeoJsonWriter();
//        System.out.println(writer.write(geometryCollection));

        String geoJSON = writer.write(geometry);
        assertEquals(
                "{\"type\":\"LineString\",\"coordinates\":[[-105.672586,38.140343],[-105.672516,38.140245],[-105.672527,38.140154],[-105.672583,38.140001],[-105.672762,38.139648],[-105.673032,38.139241],[-105.673156,38.139015],[-105.673242,38.138483],[-105.673302,38.137599],[-105.673254,38.137266],[-105.673147,38.136789],[-105.672982,38.136249],[-105.672807,38.135854],[-105.672634,38.135629],[-105.672541,38.135396],[-105.672493,38.135225],[-105.6724,38.134955],[-105.672305,38.134595],[-105.672211,38.134325],[-105.671969,38.134002],[-105.671691,38.133589],[-105.671053,38.132412],[-105.670832,38.132026],[-105.670324,38.131325],[-105.670232,38.1312],[-105.670094,38.13112],[-105.669945,38.131048],[-105.669807,38.130968],[-105.669715,38.130856],[-105.669691,38.13077],[-105.669725,38.130689],[-105.669781,38.130563],[-105.669927,38.130327],[-105.669972,38.130237],[-105.66997,38.13003],[-105.669943,38.129597],[-105.669907,38.129408],[-105.669917,38.129264],[-105.669984,38.129092],[-105.670055,38.128661]],\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:0\"}}}",
                geoJSON,
                "geoJSON line string"
        );
    }

    @Test
    void coloradoTest() throws Exception {
        final String filename = TestHelper.COLORADO_SHAPEFILE_BASE_NAME;
        TestHelper testHelper = new TestHelper();

        InputStream mainInputStream = testHelper.getMainInputStream(filename);
        Shapefile shapefile = new Shapefile(mainInputStream);

        // Read the entire geometry collection from the shapefile.
        GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
        mainInputStream.close();

        assertNotNull(geometryCollection, "geometry collection");
        int nGeometries = geometryCollection.getNumGeometries();
        assertEquals(63, nGeometries, "n geometries");

        final int recordIndex = 60;
        Geometry geometry = geometryCollection.getGeometryN(recordIndex);
        assertEquals(
                "POLYGON ((-107.47185516357422 36.99877166748047, -108.37183380126953 36.999473571777344, -108.36638641357422 37.019710540771484, -108.32830810546875 37.0582389831543, -108.31259155273438 37.10139846801758, -108.28411102294922 37.14436340332031, -108.2918472290039 37.16574478149414, -108.28955078125 37.21041488647461, -108.24185943603516 37.2467155456543, -108.19620513916016 37.33832550048828, -108.11116027832031 37.38025665283203, -108.07246398925781 37.43507385253906, -108.03474426269531 37.45528793334961, -108.01219177246094 37.58235549926758, -107.9652328491211 37.63120651245117, -107.47590637207031 37.629425048828125, -107.4769058227539 37.42544174194336, -107.47185516357422 36.99877166748047))",
                geometry.toString(),
                "geometry string"
        );

        DBASEReader reader = new DBASEReader();
        InputStream dbfStream = testHelper.getDbfInputStream(filename);
        List<DBField> fields = reader.readRecord(dbfStream, recordIndex);
        dbfStream.close();
        assertEquals(
                "[DBRecord{name='NAME', type='Ń', value=null}, DBRecord{name='CNTY_FIPS', type='⅃', value=null}, DBRecord{name='FIPS', type='⑃', value=null}]",
                Arrays.toString(fields.toArray()),
                "database record"
        );

    }

    @Test
    void citiesTest() throws Exception {
        final String filename = TestHelper.CITIES_SHAPEFILE_BASE_NAME;
        TestHelper testHelper = new TestHelper();

        InputStream mainInputStream = testHelper.getMainInputStream(filename);
        Shapefile shapefile = new Shapefile(mainInputStream);

        // Read the entire geometry collection from the shapefile.
        GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
        mainInputStream.close();

        assertNotNull(geometryCollection, "geometry collection");
        int nGeometries = geometryCollection.getNumGeometries();
        assertEquals(35, nGeometries, "n geometries");

        final int recordIndex = 3;
        Geometry geometry = geometryCollection.getGeometryN(recordIndex);
        assertEquals(
                "POINT (-122.2714765517 37.81045118311)",
                geometry.toString(),
                "geometry string"
        );

        DBASEReader reader = new DBASEReader();
        InputStream dbfStream = testHelper.getDbfInputStream(filename);
        List<DBField> fields = reader.readRecord(dbfStream, recordIndex);
        dbfStream.close();
        assertEquals(
                "[DBRecord{name='AREA', type='N', value=0}, DBRecord{name='PERIMETER', type='N', value=0}, DBRecord{name='CITIES_', type='N', value=35}, DBRecord{name='CITIES_ID', type='N', value=70}, DBRecord{name='CITY_NAME', type='C', value=Oakland}, DBRecord{name='GMI_ADMIN', type='C', value=USA-CAL}, DBRecord{name='ADMIN_NAME', type='C', value=California}, DBRecord{name='FIPS_CNTRY', type='C', value=US}, DBRecord{name='CNTRY_NAME', type='C', value=United States}, DBRecord{name='STATUS', type='C', value=Other}, DBRecord{name='POP_RANK', type='N', value=4}, DBRecord{name='POP_CLASS', type='C', value=250,000 to 500,000}, DBRecord{name='PORT_ID', type='N', value=16340}]",
                Arrays.toString(fields.toArray()),
                "database record"
        );

    }

}
