package com.primalimited.gis;

import com.mapbox.geojson.Feature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.function.Consumer;

class PointHandler implements ShapeHandler {
    int Ncoords=2; //2 = x,y ;  3= x,y,m ; 4 = x,y,z,m
    int myShapeType = -1;

    PointHandler(int type) throws InvalidShapefileException
    {
        if ( (type != 1) && (type != 11) && (type != 21))// 2d, 2d+m, 3d+m
            throw new InvalidShapefileException("PointHandler constructor: expected a type of 1, 11 or 21");
        myShapeType = type;
    }

    PointHandler()
    {
        myShapeType = 1; //2d
    }

    @Override
    public void streamFeature(EndianDataInputStream file, int recordIndex, int contentLength, Consumer<Feature> consumer) throws IOException, InvalidShapefileException {
        //TODO
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void stream(EndianDataInputStream file, GeometryFactory geometryFactory, int recordIndex, int contentLength, Consumer<Geometry> consumer) throws IOException, InvalidShapefileException {
        //  file.setLittleEndianMode(true);
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType != myShapeType)
            throw new InvalidShapefileException("pointhandler.read() - handler's shapetype doesn't match file's");

        double x = file.readDoubleLE();
        double y = file.readDoubleLE();
        double m , z = Double.NaN;
        actualReadWords += 8;

        if ( shapeType ==11 )
        {
            z= file.readDoubleLE();
            actualReadWords += 4;
        }
        if ( shapeType >=11 )
        {
            m = file.readDoubleLE();
            actualReadWords += 4;
        }

        //verify that we have read everything we need
        while (actualReadWords < contentLength)
        {
            int junk2 = file.readShortBE();
            actualReadWords += 1;
        }

        Geometry geometry = geometryFactory.createPoint(new Coordinate(x,y,z));
        geometry.setUserData(recordIndex);
        consumer.accept(geometry);
    }

    @Override
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws IOException,InvalidShapefileException {
        //  file.setLittleEndianMode(true);
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType != myShapeType)
            throw new InvalidShapefileException("pointhandler.read() - handler's shapetype doesn't match file's");

        double x = file.readDoubleLE();
        double y = file.readDoubleLE();
        double m , z = Double.NaN;
        actualReadWords += 8;

        if ( shapeType ==11 )
        {
            z= file.readDoubleLE();
            actualReadWords += 4;
        }
        if ( shapeType >=11 )
        {
            m = file.readDoubleLE();
            actualReadWords += 4;
        }

        //verify that we have read everything we need
        while (actualReadWords < contentLength)
        {
            int junk2 = file.readShortBE();
            actualReadWords += 1;
        }

        return geometryFactory.createPoint(new Coordinate(x,y,z));
    }

    /**
     * Returns the shapefile shape type value for a point
     * @return int Shapefile.POINT
     */
    @Override
    public int getShapeType(){
        return myShapeType;
    }

    /**
     * Calcuates the record length of this object.
     * @return int The length of the record that this shapepoint will take up in a shapefile
     **/
    @Override
    public int getLength(Geometry geometry) {
        if (myShapeType == Shapefile.POINT)
            return 10;
        if (myShapeType == Shapefile.POINTM)
            return 14;

        return 18;
    }
}
