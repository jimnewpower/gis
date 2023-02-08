package com.primalimited.gis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class IndexFile {

    public Pair<Integer, Integer> readOffsetAndLengthAsNumberOf16BitWords(InputStream inputStream, int recordNumber) throws IOException {
        IntBuffer intBuffer = read(inputStream, recordNumber);
        return Pair.with(intBuffer.get(), intBuffer.get());
    }

    public Pair<Integer, Integer> readOffsetAndLengthAsNumberOfBytes(InputStream inputStream, int recordNumber) throws IOException {
        IntBuffer intBuffer = read(inputStream, recordNumber);
        return Pair.with(intBuffer.get() * 2, intBuffer.get() * 2);
    }

    public IntBuffer read(InputStream inputStream, int recordNumber) throws IOException {
        int nBytesToSkip = ShapefileConstants.N_HEADER_BYTES + (ShapefileConstants.RECORD_HEADER_LENGTH * (recordNumber-1));
        inputStream.readNBytes(nBytesToSkip);
        byte[] bytes = inputStream.readNBytes(ShapefileConstants.RECORD_HEADER_LENGTH);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        return byteBuffer.asIntBuffer();
    }
}
