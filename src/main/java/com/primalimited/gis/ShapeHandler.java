package com.primalimited.gis;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

interface ShapeHandler {
    int getShapeType();
    Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws java.io.IOException,InvalidShapefileException;
    int getLength(Geometry geometry); //length in 16bit words
}
