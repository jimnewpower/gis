package com.primalimited.gis;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Precision model singleton with 1.1cm precision for decimal lat/long values.
 * This is useful when writing text files with locations (e.g. GeoJSON), to
 * keep the files as small as possible.
 */
public enum PrecisionModelLatLong {
    INSTANCE;

    // 6 digits of precision for lat/long values is 11.1 cm.
    private final double SCALE = 1e6;

    private final PrecisionModel precisionModel;

    PrecisionModelLatLong() {
        this.precisionModel = new PrecisionModel(SCALE);
    }

    public double makePrecise(double value) {
        return precisionModel.makePrecise(value);
    }

    public Coordinate createCoordinate(double x, double y) {
        return new Coordinate(makePrecise(x), makePrecise(y));
    }
}
