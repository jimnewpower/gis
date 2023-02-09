package com.primalimited.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {

    @Test
    void clientTest() throws Exception {
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
}
