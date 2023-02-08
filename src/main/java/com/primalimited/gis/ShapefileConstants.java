package com.primalimited.gis;

class ShapefileConstants {
    // these files are required
    public static final String SHAPEFILE_EXTENSION = "shp";
    public static final String SHAPEFILE_INDEX_EXTENSION = "shx";
    public static final String SHAPEFILE_DBF_EXTENSION = "dbf";
    // this one is optional
    public static final String SHAPEFILE_PROJECTION_EXTENSION = "prj";

    public static final String EDITED_DBF_FILE_SUFFIX = "-EDIT";

    // shape type constants
    public static final int SHAPE_TYPE_NULL = 0;
    public static final int SHAPE_TYPE_POINT = 1;
    public static final int SHAPE_TYPE_POLYLINE = 3;
    public static final int SHAPE_TYPE_POLYGON = 5;
    public static final int SHAPE_TYPE_MULTIPOINT = 8;
    public static final int SHAPE_TYPE_POINTZ = 11;
    public static final int SHAPE_TYPE_POLYLINEZ = 13;
    public static final int SHAPE_TYPE_POLYGONZ = 15;
    public static final int SHAPE_TYPE_MULTIPOINTZ = 18;
    public static final int SHAPE_TYPE_POINTM = 21;
    public static final int SHAPE_TYPE_POLYLINEM = 23;
    public static final int SHAPE_TYPE_POLYGONM = 25;
    public static final int SHAPE_TYPE_MULTIPOINTM = 28;
    public static final int SHAPE_TYPE_MULTIPATCH = 31;

    public static enum BasicShapeType {
        POINT,
        LINE,
        POLYGON,
        MULTIPATCH,
        UNKOWN;

        public static BasicShapeType basicShapeTypeFromShapeTypeValue(int shapeType) {
            if (shapeType == SHAPE_TYPE_NULL)
                return UNKOWN;

            if (shapeType == SHAPE_TYPE_POINT)
                return POINT;
            if (shapeType == SHAPE_TYPE_MULTIPOINT)
                return POINT;
            if (shapeType == SHAPE_TYPE_POINTZ)
                return POINT;
            if (shapeType == SHAPE_TYPE_MULTIPOINTZ)
                return POINT;
            if (shapeType == SHAPE_TYPE_POINTM)
                return POINT;
            if (shapeType == SHAPE_TYPE_MULTIPOINTM)
                return POINT;

            if (shapeType == SHAPE_TYPE_POLYLINE)
                return LINE;
            if (shapeType == SHAPE_TYPE_POLYLINEZ)
                return LINE;
            if (shapeType == SHAPE_TYPE_POLYLINEM)
                return LINE;

            if (shapeType == SHAPE_TYPE_POLYGON)
                return POLYGON;
            if (shapeType == SHAPE_TYPE_POLYGONZ)
                return POLYGON;
            if (shapeType == SHAPE_TYPE_POLYGONM)
                return POLYGON;

            if (shapeType == SHAPE_TYPE_MULTIPATCH)
                return MULTIPATCH;

            return UNKOWN;
        }
    }

    // header constants
    public static final int N_HEADER_BYTES = 100;
    public static final int HEADER_FILE_CODE = 9994;/*this is hardwired in the spec*/
    public static final int BIG_ENDIAN_INITIAL_HEADER_POSITION = 0;
    public static final int BIG_ENDIAN_TOTAL_HEADER_BYTES = 28;
    public static final int LITTLE_ENDIAN_INITIAL_HEADER_POSITION = BIG_ENDIAN_TOTAL_HEADER_BYTES;
    public static final int LITTLE_ENDIAN_DOUBLE_VALUES_POSITION = BIG_ENDIAN_TOTAL_HEADER_BYTES + 8;
    public static final int LITTLE_ENDIAN_TOTAL_HEADER_BYTES = N_HEADER_BYTES - BIG_ENDIAN_TOTAL_HEADER_BYTES;

    public static final int RECORD_HEADER_LENGTH = 8;
}
