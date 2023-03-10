package com.primalimited.gis;

import com.mapbox.geojson.Feature;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.function.Consumer;

interface ShapeHandler {
    int getShapeType();
    Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws java.io.IOException,InvalidShapefileException;
    int getLength(Geometry geometry); //length in 16bit words

    /**
     * Stream the data out of the file, using a geometry consumer to consume the data.
     *
     * @param file the input stream.
     * @param geometryFactory the geometry factory.
     * @param recordIndex record index (0-based).
     * @param contentLength content length.
     * @param consumer geometry consumer.
     * @throws java.io.IOException
     * @throws InvalidShapefileException
     */
    void stream(
            EndianDataInputStream file,
            GeometryFactory geometryFactory,
            int recordIndex,
            int contentLength,
            Consumer<Geometry> consumer
    ) throws java.io.IOException,InvalidShapefileException;

    void streamFeature(
            EndianDataInputStream file,
            int recordIndex,
            int contentLength,
            Consumer<Feature> consumer
    ) throws java.io.IOException,InvalidShapefileException;
}
