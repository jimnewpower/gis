package com.primalimited.gis;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

class PrecisionModelLatLongTest {

    @Test
    void precisionModelLatLongTest() {
        PrecisionModelLatLong precisionModel = PrecisionModelLatLong.INSTANCE;
        double value = 0.123456789101112;
        double precise = precisionModel.makePrecise(value);
        assertEquals(0.123457, precise, 1e-9);
        assertEquals("0.123457", String.valueOf(precise));
    }

    @Test
    void createCoordinateTest() {
        double x = 987654.3210123456789;
        double y = 543210.1234567891011;
        Coordinate c = PrecisionModelLatLong.INSTANCE.createCoordinate(x, y);
        assertEquals(987654.321012, c.getX(), 1e-9);
        assertEquals(543210.123457, c.getY(), 1e-9);
        assertEquals("987654.321012", String.valueOf(c.getX()));
        assertEquals("543210.123457", String.valueOf(c.getY()));
    }
}