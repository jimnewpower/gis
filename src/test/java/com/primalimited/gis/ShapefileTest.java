package com.primalimited.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.kml.KMLWriter;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShapefileTest {

    @Test
    void readGeometriesTest() throws Exception {
        TestHelper testHelper = new TestHelper();

        InputStream is = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(is);
        GeometryCollection gc = shapefile.read(new GeometryFactory());
        is.close();

        assertNotNull(gc, "geometry collection returned from shapefile read().");
        assertEquals(3072, gc.getNumGeometries(), "number of records read from shapefile");
        Geometry geometry = gc.getGeometryN(0);
        assertEquals("LINESTRING (-103.1118726555984 37.32598219831266, -103.11210161809805 37.325475495188414, -103.11219296809787 37.32350857748315, -103.11198194518153 37.32183963998574, -103.11185533164007 37.32100652748704, -103.11203261080647 37.31981487748891, -103.11221706080619 37.31917850144822, -103.11256700768064 37.31866255040734, -103.11315115142975 37.31793525770013, -103.11373810663719 37.31731850249275, -103.11402486809504 37.316801531660246, -103.11396096288684 37.31657851082724, -103.11390862017856 37.31639591082751, -103.11400503684507 37.31590955874492, -103.1143915983028 37.31561288582873, -103.11484060767714 37.31534540978748, -103.11538842642625 37.31494793895479, -103.11620402746667 37.31434924416402, -103.11650656600784 37.314172307705974, -103.11672666600754 37.31391651603968, -103.11691702329887 37.31366072333179, -103.11700030454875 37.313393033748866, -103.11702409829871 37.31311344729096, -103.11711278684027 37.312880024374635, -103.1171680201735 37.31276185041651, -103.11721939934012 37.3126539525, -103.11731188371493 37.312539632708535, -103.11735812538154 37.31247412333363, -103.11748143683968 37.31230328479222, -103.11752896392295 37.31225062020894, -103.11745446288137 37.31210033479255, -103.11743005767312 37.31194105666776, -103.11739147121483 37.311816511876316, -103.11738818267315 37.311728549376426, -103.11738160558986 37.311620857709954, -103.11735940975655 37.31141698166857, -103.11735694413153 37.31131504416874, -103.11729364413162 37.31100429833589, -103.11724103059004 37.31089331812774, -103.11719746079848 37.31078891396123, -103.11716622121514 37.31070095146134, -103.1170850587153 37.31052912333661, -103.11707218371532 37.310329162920254, -103.11707922225696 37.309202071255356, -103.11721898059011 37.30866593271452, -103.11776378371422 37.30793435667397, -103.11831933267172 37.307238368133426, -103.11946460558659 37.305803557718946, -103.12016689412718 37.305314390011404, -103.12121438995888 37.30469171501238, -103.12245050245696 37.30410569001327, -103.12309401078932 37.303858246263644, -103.12341167328879 37.30356574938912, -103.12355383474693 37.30323192334794, -103.12372478787165 37.30283046918191, -103.123927210788 37.302312133766065, -103.12490396182818 37.301991148349884, -103.12604703890975 37.3017568941836, -103.12708607328312 37.301470522309046, -103.1276401055739 37.30121010876775, -103.12853095973918 37.30095502335149, -103.12926777015474 37.30096667647649, -103.13086478369388 37.3011096775179, -103.13104638473527 37.301144126476174, -103.13212248994193 37.301348250434216, -103.13306649723216 37.30149774626733, -103.13326489723187 37.30155146501721, -103.13392422118915 37.30172998793364, -103.1348471941044 37.301879140016695, -103.13595671701933 37.30214898064128, -103.13693912118447 37.302450462932484, -103.1379313784746 37.30320633376465, -103.13870238159842 37.303773684805435, -103.13938475034735 37.304053573346664, -103.13996145242982 37.30372616084719, -103.14052746180391 37.30297798272335, -103.14117560138624 37.30231520668269, -103.14210991388478 37.30150198585062, -103.14300392950838 37.300762469185145, -103.14343861700775 37.30021410981095, -103.14371273263231 37.29934356502065, -103.14393399096531 37.298311799397254, -103.14425106179812 37.29714806919071, -103.1443960128396 37.296951332732704, -103.1448392711722 37.29634974523361, -103.14525038367157 37.295901941067655, -103.14532406804648 37.29547408169333, -103.14529890137982 37.29537617023516, -103.14518951804666 37.29495044419417, -103.14510903158845 37.2947977598194, -103.14553664408783 37.29453527023645, -103.14680967846084 37.29345587232149, -103.14719257637688 37.29313117440529, -103.14781662950094 37.29258575982283, -103.1495261565816 37.29156942336607, -103.1506172044966 37.291090419200145, -103.15143920241195 37.29107904628347, -103.15220806178576 37.291258034824864, -103.15290351386801 37.291235240033245, -103.15341363678391 37.291041318158534, -103.15358778574199 37.290836796283884, -103.15373882636675 37.29065944107583, -103.15408300969955 37.29036198170127, -103.1554288232391 37.28957541086913, -103.15647171177915 37.28912059628652, -103.15742752115267 37.28878217337041, -103.15779765656879 37.28828322337114, -103.15796263261018 37.28756235670562, -103.15808099094335 37.287025833789755, -103.1586777284424 37.286732282748574, -103.15952785239944 37.28640902754074, -103.1596983794825 37.286324362957544, -103.1603164523982 37.286017520249686, -103.16072537218923 37.285653749416895, -103.16095345448053 37.284933859834666, -103.16098415864718 37.28367253379497, -103.16098768052217 37.28266315150489, -103.16065794198101 37.28176333900626, -103.16061254718937 37.281639466089814, -103.16053187427286 37.28062876713307, -103.16069189093929 37.2801097129672, -103.16119191906353 37.27946134421819, -103.16229390135345 37.27830077546997, -103.16338972739345 37.27739246505473, -103.16394467843423 37.27708142026353, -103.16477648155796 37.27664007234756, -103.16560500864 37.276333260889714, -103.16628417113895 37.27610825776503, -103.16658892322181 37.27551181818262, -103.16681676384644 37.274831179642035, -103.16702761488779 37.27410088693483, -103.16736448676227 37.27323125151952, -103.16757466801192 37.27237646506251, -103.16759056905357 37.27172058068851, -103.16750302738706 37.27118261298102)", geometry.toText());
    }

    @Test
    void readSpecificRecordBothFilesTest() throws Exception {
        final int recordNumber = 4;

        TestHelper testHelper = new TestHelper();

        InputStream mainFileInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainFileInputStream);
        GeometryCollection gc = shapefile.read(new GeometryFactory());
        mainFileInputStream.close();


        assertNotNull(gc, "geometry collection returned from shapefile read().");
        assertEquals(3072, gc.getNumGeometries(), "number of records read from shapefile");
        Geometry geometry = gc.getGeometryN(recordNumber);
        assertEquals("LINESTRING (-105.67258583183201 38.14034331371528, -105.6725164557904 38.1402445730904, -105.67252705058206 38.14015438559056, -105.67258269849867 38.14000084600747, -105.67276205683169 38.13964833038301, -105.67303219953965 38.13924120850862, -105.67315564849775 38.13901518767568, -105.67324206933097 38.13848293455146, -105.67330242870588 38.13759935017788, -105.67325374641428 38.13726615642838, -105.67314667558111 38.13678909809579, -105.67298197662302 38.1362492710133, -105.67280719537331 38.135853719972204, -105.67263398599857 38.13562939809759, -105.67254055474871 38.1353955991396, -105.67249335474878 38.13522463038987, -105.6723995912073 38.13495478143193, -105.67230501516576 38.13459481268251, -105.67221126829088 38.13432497309958, -105.67196869641629 38.13400190018342, -105.67169107037506 38.133588902267434, -105.67105275579269 38.13241188664426, -105.67083243600138 38.132025587686485, -105.67032399746051 38.131325493937595, -105.67023157871063 38.13119985435446, -105.67009392350252 38.131119526229554, -105.6699449412111 38.13104826685469, -105.6698072880863 38.1309679481048, -105.66971498287813 38.13085580956334, -105.66969139225313 38.13077032518845, -105.66972487975306 38.13068901998025, -105.66978077350302 38.13056252623045, -105.66992696516945 38.130327356439125, -105.66997177871104 38.13023697518929, -105.66996989537773 38.130029698106284, -105.6699431349611 38.12959723873195, -105.66990717662782 38.1294081710239, -105.66991726829445 38.12926391269082, -105.66998416933603 38.12909229185772, -105.67005476725257 38.128661063733375)", geometry.toText());

        InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOf16BitWords(indexFileInputStream, recordNumber);
        indexFileInputStream.close();
        int offset = pair.getAValue();
        int length = pair.getBValue();
        assertEquals(offset, 1534);
        assertEquals(length, 416);


        InputStream dbInputStream = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        List<DBField> fields = new DBASEReader()
                .readRecord(dbInputStream, testHelper.readDbfHeader(TestHelper.LINE_SHAPEFILE_BASE_NAME), recordNumber);
        dbInputStream.close();

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

        assertEquals(8, fields.size(), "number of fields");
        for (int i = 0; i < fields.size(); i++) {
            assertEquals(expected[i], fields.get(i).toString(), "field " + i + " toString()");
        }
    }

    @Test
    public void testKMLWriter() throws Exception {
        final int recordNumber = 4;

        TestHelper testHelper = new TestHelper();

        InputStream mainFileInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainFileInputStream);
        GeometryCollection gc = shapefile.read(new GeometryFactory());
        mainFileInputStream.close();


        assertNotNull(gc, "geometry collection returned from shapefile read().");
        assertEquals(3072, gc.getNumGeometries(), "number of records read from shapefile");
        Geometry geometry = gc.getGeometryN(recordNumber);

        KMLWriter kmlWriter = new KMLWriter();
        kmlWriter.setPrecision(6);
        String kmlString = kmlWriter.write(geometry);

        final String expected =
"""
<LineString>
  <coordinates>-105.672586,38.140343 -105.672516,38.140245 -105.672527,38.140154 -105.672583,38.140001 -105.672762,38.139648
     -105.673032,38.139241 -105.673156,38.139015 -105.673242,38.138483 -105.673302,38.137599 -105.673254,38.137266
     -105.673147,38.136789 -105.672982,38.136249 -105.672807,38.135854 -105.672634,38.135629 -105.672541,38.135396
     -105.672493,38.135225 -105.6724,38.134955 -105.672305,38.134595 -105.672211,38.134325 -105.671969,38.134002
     -105.671691,38.133589 -105.671053,38.132412 -105.670832,38.132026 -105.670324,38.131325 -105.670232,38.1312
     -105.670094,38.13112 -105.669945,38.131048 -105.669807,38.130968 -105.669715,38.130856 -105.669691,38.13077
     -105.669725,38.130689 -105.669781,38.130563 -105.669927,38.130327 -105.669972,38.130237 -105.66997,38.13003
     -105.669943,38.129597 -105.669907,38.129408 -105.669917,38.129264 -105.669984,38.129092 -105.670055,38.128661</coordinates>
</LineString>
""";
        assertEquals(expected, kmlString, "KML String");
    }
}