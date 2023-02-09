package com.primalimited.gis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Methods that operate on an ESRI shapefile index (.shx) file.
 */
public class IndexFile {

    /**
     * Read offsets and lengths where the values represent the number of 16-bit words (as-is).
     *
     * @param inputStream the .shx file input stream.
     * @param recordNumber which record number to read from the index file.
     * @return pair of (offset, length). See shapefile specification.
     * @throws IOException
     */
    public Pair<Integer, Integer> readOffsetAndLengthAsNumberOf16BitWords(InputStream inputStream, int recordNumber) throws IOException {
        IntBuffer intBuffer = read(inputStream, recordNumber);
        return Pair.with(intBuffer.get(), intBuffer.get());
    }

    /**
     * Read offsets and lengths where the values represent number of bytes.
     *
     * @param inputStream the .shx file input stream.
     * @param recordNumber which record number to read from the index file.
     * @return pair of (offset, length) in bytes. See shapefile specification.
     * @throws IOException
     */
    public Pair<Integer, Integer> readOffsetAndLengthAsNumberOfBytes(InputStream inputStream, int recordNumber) throws IOException {
        IntBuffer intBuffer = read(inputStream, recordNumber);
        return Pair.with(intBuffer.get() * 2, intBuffer.get() * 2);
    }

    private IntBuffer read(InputStream inputStream, int recordNumber) throws IOException {
        int nBytesToSkip = ShapefileConstants.N_HEADER_BYTES + (ShapefileConstants.RECORD_HEADER_LENGTH * (recordNumber-1));
        inputStream.readNBytes(nBytesToSkip);
        byte[] bytes = inputStream.readNBytes(ShapefileConstants.RECORD_HEADER_LENGTH);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        return byteBuffer.asIntBuffer();
    }
}
