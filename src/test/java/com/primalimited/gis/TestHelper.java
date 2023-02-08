package com.primalimited.gis;

import java.io.IOException;
import java.io.InputStream;

public class TestHelper {
    static String LINE_SHAPEFILE_BASE_NAME = "WBDLine";

    InputStream getMainInputStream(String baseFilename) {
        return getClass()
                .getClassLoader()
                .getResourceAsStream(baseFilename + "." + ShapefileConstants.SHAPEFILE_EXTENSION);
    }

    InputStream getIndexInputStream(String baseFilename) {
        return getClass()
                .getClassLoader()
                .getResourceAsStream(baseFilename + "." + ShapefileConstants.SHAPEFILE_INDEX_EXTENSION);
    }

    InputStream getDbfInputStream(String baseFilename) {
        return getClass()
                .getClassLoader()
                .getResourceAsStream(baseFilename + "." + ShapefileConstants.SHAPEFILE_DBF_EXTENSION);
    }

    InputStream getPrjInputStream(String baseFilename) {
        return getClass()
                .getClassLoader()
                .getResourceAsStream(baseFilename + "." + ShapefileConstants.SHAPEFILE_PROJECTION_EXTENSION);
    }

    DBASEReader.DBASEHeaderInfo readDbfHeader(String baseFilename) throws IOException {
        InputStream is = getDbfInputStream(baseFilename);
        DBASEReader reader = new DBASEReader(is);

        DBASEReader.DBASEHeaderInfo data = reader.readHeader();
        is.close();
        return data;
    }
}
