package com.primalimited.gis;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.*;

class IndexFileTest {

    @Test
    void pairTestAs16BitWords() throws IOException {
        TestHelper testHelper = new TestHelper();

        final int nRecords = 52;
        for (int recordNumber = 0; recordNumber < nRecords; recordNumber++) {
            InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
            Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOf16BitWords(indexFileInputStream, recordNumber);
            int offset = pair.getAValue();
            int length = pair.getBValue();
            assertEquals(offsets[recordNumber], offset, "offset for record number " + recordNumber);
            assertEquals(lengths[recordNumber], length, "length for record number " + recordNumber);
            indexFileInputStream.close();
        }

        final int recordNumber = 3071;
        InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOf16BitWords(indexFileInputStream, recordNumber);
        int offset = pair.getAValue();
        int length = pair.getBValue();
        assertEquals(3582170, offset, "offset for record number " + recordNumber);
        assertEquals(1728, length, "length for record number " + recordNumber);
        indexFileInputStream.close();
    }

    @Test
    void pairTestAsNBytes() throws IOException {
        TestHelper testHelper = new TestHelper();

        final int nRecords = 52;
        for (int recordNumber = 0; recordNumber < nRecords; recordNumber++) {
            InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
            Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOfBytes(indexFileInputStream, recordNumber);
            int offset = pair.getAValue();
            int length = pair.getBValue();
            assertEquals(offsets[recordNumber] * 2, offset, "offset for record number " + recordNumber);
            assertEquals(lengths[recordNumber] * 2, length, "length for record number " + recordNumber);
            indexFileInputStream.close();
        }

        final int recordNumber = 3071;
        InputStream indexFileInputStream = testHelper.getIndexInputStream(TestHelper.LINE_SHAPEFILE_BASE_NAME);
        Pair<Integer, Integer> pair = new IndexFile().readOffsetAndLengthAsNumberOfBytes(indexFileInputStream, recordNumber);
        int offset = pair.getAValue();
        int length = pair.getBValue();
        assertEquals(3582170 * 2, offset, "offset for record number " + recordNumber);
        assertEquals(1728 * 2, length, "length for record number " + recordNumber);
        indexFileInputStream.close();
    }

    private static final int[] offsets = new int[] {
            0,
            50,
            1158,
            1298,
            1534,
            1954,
            2302,
            2634,
            2974,
            3394,
            3846,
            4282,
            4862,
            5034,
            5334,
            5554,
            6054,
            6650,
            6718,
            7362,
            7870,
            8090,
            12454,
            12650,
            13230,
            13946,
            14374,
            14994,
            15334,
            16050,
            16598,
            17066,
            17510,
            17690,
            17806,
            18482,
            18998,
            19458,
            20070,
            20778,
            21014,
            21178,
            21870,
            22162,
            22598,
            23202,
            23838,
            24482,
            24686,
            25506,
            25894,
            26370
    };

    private static final int[] lengths = new int[]{
            0,
            1104,
            136,
            232,
            416,
            344,
            328,
            336,
            416,
            448,
            432,
            576,
            168,
            296,
            216,
            496,
            592,
            64,
            640,
            504,
            216,
            4360,
            192,
            576,
            712,
            424,
            616,
            336,
            712,
            544,
            464,
            440,
            176,
            112,
            672,
            512,
            456,
            608,
            704,
            232,
            160,
            688,
            288,
            432,
            600,
            632,
            640,
            200,
            816,
            384,
            472,
            664
    };
}