package com.primalimited.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.kml.KMLWriter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        assertEquals(
                "LINESTRING (-103.111873 37.325982, -103.112102 37.325475, -103.112193 37.323509, -103.111982 37.32184, -103.111855 37.321007, -103.112033 37.319815, -103.112217 37.319179, -103.112567 37.318663, -103.113151 37.317935, -103.113738 37.317319, -103.114025 37.316802, -103.113961 37.316579, -103.113909 37.316396, -103.114005 37.31591, -103.114392 37.315613, -103.114841 37.315345, -103.115388 37.314948, -103.116204 37.314349, -103.116507 37.314172, -103.116727 37.313917, -103.116917 37.313661, -103.117 37.313393, -103.117024 37.313113, -103.117113 37.31288, -103.117168 37.312762, -103.117219 37.312654, -103.117312 37.31254, -103.117358 37.312474, -103.117481 37.312303, -103.117529 37.312251, -103.117454 37.3121, -103.11743 37.311941, -103.117391 37.311817, -103.117388 37.311729, -103.117382 37.311621, -103.117359 37.311417, -103.117357 37.311315, -103.117294 37.311004, -103.117241 37.310893, -103.117197 37.310789, -103.117166 37.310701, -103.117085 37.310529, -103.117072 37.310329, -103.117079 37.309202, -103.117219 37.308666, -103.117764 37.307934, -103.118319 37.307238, -103.119465 37.305804, -103.120167 37.305314, -103.121214 37.304692, -103.122451 37.304106, -103.123094 37.303858, -103.123412 37.303566, -103.123554 37.303232, -103.123725 37.30283, -103.123927 37.302312, -103.124904 37.301991, -103.126047 37.301757, -103.127086 37.301471, -103.12764 37.30121, -103.128531 37.300955, -103.129268 37.300967, -103.130865 37.30111, -103.131046 37.301144, -103.132122 37.301348, -103.133066 37.301498, -103.133265 37.301551, -103.133924 37.30173, -103.134847 37.301879, -103.135957 37.302149, -103.136939 37.30245, -103.137931 37.303206, -103.138702 37.303774, -103.139385 37.304054, -103.139961 37.303726, -103.140527 37.302978, -103.141176 37.302315, -103.14211 37.301502, -103.143004 37.300762, -103.143439 37.300214, -103.143713 37.299344, -103.143934 37.298312, -103.144251 37.297148, -103.144396 37.296951, -103.144839 37.29635, -103.14525 37.295902, -103.145324 37.295474, -103.145299 37.295376, -103.14519 37.29495, -103.145109 37.294798, -103.145537 37.294535, -103.14681 37.293456, -103.147193 37.293131, -103.147817 37.292586, -103.149526 37.291569, -103.150617 37.29109, -103.151439 37.291079, -103.152208 37.291258, -103.152904 37.291235, -103.153414 37.291041, -103.153588 37.290837, -103.153739 37.290659, -103.154083 37.290362, -103.155429 37.289575, -103.156472 37.289121, -103.157428 37.288782, -103.157798 37.288283, -103.157963 37.287562, -103.158081 37.287026, -103.158678 37.286732, -103.159528 37.286409, -103.159698 37.286324, -103.160316 37.286018, -103.160725 37.285654, -103.160953 37.284934, -103.160984 37.283673, -103.160988 37.282663, -103.160658 37.281763, -103.160613 37.281639, -103.160532 37.280629, -103.160692 37.28011, -103.161192 37.279461, -103.162294 37.278301, -103.16339 37.277392, -103.163945 37.277081, -103.164776 37.27664, -103.165605 37.276333, -103.166284 37.276108, -103.166589 37.275512, -103.166817 37.274831, -103.167028 37.274101, -103.167364 37.273231, -103.167575 37.272376, -103.167591 37.271721, -103.167503 37.271183)",
                geometry.toText()
        );
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
        assertEquals(
                "LINESTRING (-105.672586 38.140343, -105.672516 38.140245, -105.672527 38.140154, -105.672583 38.140001, -105.672762 38.139648, -105.673032 38.139241, -105.673156 38.139015, -105.673242 38.138483, -105.673302 38.137599, -105.673254 38.137266, -105.673147 38.136789, -105.672982 38.136249, -105.672807 38.135854, -105.672634 38.135629, -105.672541 38.135396, -105.672493 38.135225, -105.6724 38.134955, -105.672305 38.134595, -105.672211 38.134325, -105.671969 38.134002, -105.671691 38.133589, -105.671053 38.132412, -105.670832 38.132026, -105.670324 38.131325, -105.670232 38.1312, -105.670094 38.13112, -105.669945 38.131048, -105.669807 38.130968, -105.669715 38.130856, -105.669691 38.13077, -105.669725 38.130689, -105.669781 38.130563, -105.669927 38.130327, -105.669972 38.130237, -105.66997 38.13003, -105.669943 38.129597, -105.669907 38.129408, -105.669917 38.129264, -105.669984 38.129092, -105.670055 38.128661)",
                geometry.toText()
        );

        InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOf16BitWords(indexFileInputStream, recordNumber);
        indexFileInputStream.close();
        int offset = pair.getAValue();
        int length = pair.getBValue();
        assertEquals(offset, 1534);
        assertEquals(length, 416);


        InputStream dbInputStream = testHelper.getDbfInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        List<DBField> fields = new DBASEReader()
                .readRecord(dbInputStream, recordNumber);
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
    void streamTest() throws Exception {
        TestHelper testHelper = new TestHelper();

        AtomicInteger counter = new AtomicInteger();
        InputStream mainFileInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainFileInputStream);
        shapefile.stream(new GeometryFactory(), (geometry) -> counter.set(counter.get()+1));
        mainFileInputStream.close();
        assertEquals(3072, counter.get());
    }

    @Test
    void testAsyncRead() throws InterruptedException, ExecutionException {
        TestHelper testHelper = new TestHelper();
        InputStream mainFileInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Shapefile shapefile = new Shapefile(mainFileInputStream);

        Callable<GeometryCollection> readLines = () -> {
            List<Geometry> geometries = new ArrayList<>();
            shapefile.stream(new GeometryFactory(), (geometry) -> geometries.add(geometry));
            mainFileInputStream.close();
            return new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<GeometryCollection> future = executorService.submit(readLines);

        CountDownLatch lock = new CountDownLatch(1);
        lock.await(2, TimeUnit.SECONDS);

        executorService.shutdownNow();

        assertEquals(3072, future.get().getNumGeometries());
    }

//    @Test
//    public void testKMLWriter() throws Exception {
//        final int recordNumber = 4;
//
//        TestHelper testHelper = new TestHelper();
//
//        InputStream mainFileInputStream = testHelper.getMainInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
//        Shapefile shapefile = new Shapefile(mainFileInputStream);
//        GeometryCollection gc = shapefile.read(new GeometryFactory());
//        mainFileInputStream.close();
//
//
//        assertNotNull(gc, "geometry collection returned from shapefile read().");
//        assertEquals(3072, gc.getNumGeometries(), "number of records read from shapefile");
//        Geometry geometry = gc.getGeometryN(recordNumber);
//
//        KMLWriter kmlWriter = new KMLWriter();
//        kmlWriter.setPrecision(6);
//        String kmlString = kmlWriter.write(geometry);
//
//        final String expected =
//"""
//<LineString>
//  <coordinates>-105.672586,38.140343 -105.672516,38.140245 -105.672527,38.140154 -105.672583,38.140001 -105.672762,38.139648
//     -105.673032,38.139241 -105.673156,38.139015 -105.673242,38.138483 -105.673302,38.137599 -105.673254,38.137266
//     -105.673147,38.136789 -105.672982,38.136249 -105.672807,38.135854 -105.672634,38.135629 -105.672541,38.135396
//     -105.672493,38.135225 -105.6724,38.134955 -105.672305,38.134595 -105.672211,38.134325 -105.671969,38.134002
//     -105.671691,38.133589 -105.671053,38.132412 -105.670832,38.132026 -105.670324,38.131325 -105.670232,38.1312
//     -105.670094,38.13112 -105.669945,38.131048 -105.669807,38.130968 -105.669715,38.130856 -105.669691,38.13077
//     -105.669725,38.130689 -105.669781,38.130563 -105.669927,38.130327 -105.669972,38.130237 -105.66997,38.13003
//     -105.669943,38.129597 -105.669907,38.129408 -105.669917,38.129264 -105.669984,38.129092 -105.670055,38.128661</coordinates>
//</LineString>
//""";
//        assertEquals(expected, kmlString, "KML String");
//    }
}