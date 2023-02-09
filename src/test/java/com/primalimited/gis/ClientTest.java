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

        InputStream mainInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainInputStream);

        // Read the entire geometry collection from the shapefile.
        GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
        mainInputStream.close();

        // User clicks on a polyline on the map, returning the unique index into the geometry collection.
        final int selectedRecordIndex = 4;

        // Select the specific geometry from the record index.
        Geometry geometry = geometryCollection.getGeometryN(selectedRecordIndex);
        assertEquals("LINESTRING (-105.67258583183201 38.14034331371528, -105.6725164557904 38.1402445730904, -105.67252705058206 38.14015438559056, -105.67258269849867 38.14000084600747, -105.67276205683169 38.13964833038301, -105.67303219953965 38.13924120850862, -105.67315564849775 38.13901518767568, -105.67324206933097 38.13848293455146, -105.67330242870588 38.13759935017788, -105.67325374641428 38.13726615642838, -105.67314667558111 38.13678909809579, -105.67298197662302 38.1362492710133, -105.67280719537331 38.135853719972204, -105.67263398599857 38.13562939809759, -105.67254055474871 38.1353955991396, -105.67249335474878 38.13522463038987, -105.6723995912073 38.13495478143193, -105.67230501516576 38.13459481268251, -105.67221126829088 38.13432497309958, -105.67196869641629 38.13400190018342, -105.67169107037506 38.133588902267434, -105.67105275579269 38.13241188664426, -105.67083243600138 38.132025587686485, -105.67032399746051 38.131325493937595, -105.67023157871063 38.13119985435446, -105.67009392350252 38.131119526229554, -105.6699449412111 38.13104826685469, -105.6698072880863 38.1309679481048, -105.66971498287813 38.13085580956334, -105.66969139225313 38.13077032518845, -105.66972487975306 38.13068901998025, -105.66978077350302 38.13056252623045, -105.66992696516945 38.130327356439125, -105.66997177871104 38.13023697518929, -105.66996989537773 38.130029698106284, -105.6699431349611 38.12959723873195, -105.66990717662782 38.1294081710239, -105.66991726829445 38.12926391269082, -105.66998416933603 38.12909229185772, -105.67005476725257 38.128661063733375)", geometry.toString());

        DBASEReader reader = new DBASEReader();
        InputStream dbfStream = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        List<DBField> fields = reader.readRecord(dbfStream, selectedRecordIndex);
        dbfStream.close();
        assertEquals(
                "[DBRecord{name='tnmid', type='C', value={E04A85C8-DAF0-4927-97E8-B2A581988AD6}}, DBRecord{name='hudigit', type='N', value=2}, DBRecord{name='humod', type='C', value=NM}, DBRecord{name='linesource', type='C', value=DRG24}, DBRecord{name='metasource', type='C', value={596E21CA-D6C3-4BEC-9C18-BF63FB3727D4}}, DBRecord{name='loaddate', type='D', value=20170918}, DBRecord{name='shape_Leng', type='N', value=0.013047043209515}, DBRecord{name='ObjectID', type='N', value=5}]",
                Arrays.toString(fields.toArray())
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
