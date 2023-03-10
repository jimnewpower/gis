package com.primalimited.gis;

import com.mapbox.geojson.Feature;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.function.Consumer;

class MultiPointHandler implements ShapeHandler {
    int myShapeType= -1;
    private PrecisionModel precisionModel = new PrecisionModel();
    private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);

    /** Creates new MultiPointHandler */
    MultiPointHandler() {
        myShapeType = 8;
    }

    MultiPointHandler(int type) throws InvalidShapefileException
    {
        if  ( (type != 8) &&  (type != 18) &&  (type != 28) )
            throw new InvalidShapefileException("Multipointhandler constructor - expected type to be 8, 18, or 28");

        myShapeType = type;
    }

    @Override
    public void streamFeature(EndianDataInputStream file, int recordIndex, int contentLength, Consumer<Feature> consumer) throws IOException, InvalidShapefileException {
        //TODO
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void stream(EndianDataInputStream file, GeometryFactory geometryFactory, int recordIndex, int contentLength, Consumer<Geometry> consumer) throws IOException, InvalidShapefileException {
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType == 0)
            consumer.accept(geometryFactory.createMultiPointFromCoords(null));
        if (shapeType != myShapeType) {
            throw new InvalidShapefileException("Multipointhandler.read() - expected type code "+myShapeType+" but got "+shapeType);
        }
        //read bbox
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();

        actualReadWords += 4*4;

        int numpoints = file.readIntLE();
        actualReadWords += 2;

        Coordinate[] coords = new Coordinate[numpoints];
        for (int t=0;t<numpoints;t++)
        {

            double x = file.readDoubleLE();
            double y = file.readDoubleLE();
            actualReadWords += 8;
            coords[t] = new Coordinate(x,y);
        }
        if (myShapeType == 18)
        {
            file.readDoubleLE(); //z min/max
            file.readDoubleLE();
            actualReadWords += 8;
            for (int t=0;t<numpoints;t++)
            {
                double z =  file.readDoubleLE();//z
                actualReadWords += 4;
                coords[t].setZ(z);
            }
        }


        if (myShapeType >= 18)
        {
            // int fullLength = numpoints * 8 + 20 +8 +4*numpoints + 8 +4*numpoints;
            int fullLength;
            if (myShapeType == 18)
            {
                //multipoint Z (with m)
                fullLength = 20 + (numpoints * 8)  +8 +4*numpoints + 8 +4*numpoints;
            }
            else
            {
                //multipoint M (with M)
                fullLength = 20 + (numpoints * 8)  +8 +4*numpoints;
            }

            if (contentLength >= fullLength)  //is the M portion actually there?
            {
                file.readDoubleLE(); //m min/max
                file.readDoubleLE();
                actualReadWords += 8;
                for (int t=0;t<numpoints;t++)
                {
                    file.readDoubleLE();//m
                    actualReadWords += 4;
                }
            }
        }

        //verify that we have read everything we need
        while (actualReadWords < contentLength)
        {
            int junk2 = file.readShortBE();
            actualReadWords += 1;
        }

        Geometry geometry = geometryFactory.createMultiPointFromCoords(coords);
        geometry.setUserData(recordIndex);
        consumer.accept(geometry);
    }

    @Override
    public Geometry read(EndianDataInputStream file,GeometryFactory geometryFactory,int contentLength) throws IOException,InvalidShapefileException{
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType ==0)
            return geometryFactory.createMultiPointFromCoords(null);
        if (shapeType != myShapeType)
        {
            throw new InvalidShapefileException("Multipointhandler.read() - expected type code "+myShapeType+" but got "+shapeType);
        }
        //read bbox
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();
        file.readDoubleLE();

        actualReadWords += 4*4;

        int numpoints = file.readIntLE();
        actualReadWords += 2;

        Coordinate[] coords = new Coordinate[numpoints];
        for (int t=0;t<numpoints;t++)
        {

            double x = file.readDoubleLE();
            double y = file.readDoubleLE();
            actualReadWords += 8;
            coords[t] = new Coordinate(x,y);
        }
        if (myShapeType == 18)
        {
            file.readDoubleLE(); //z min/max
            file.readDoubleLE();
            actualReadWords += 8;
            for (int t=0;t<numpoints;t++)
            {
                double z =  file.readDoubleLE();//z
                actualReadWords += 4;
                coords[t].setZ(z);
            }
        }


        if (myShapeType >= 18)
        {
            // int fullLength = numpoints * 8 + 20 +8 +4*numpoints + 8 +4*numpoints;
            int fullLength;
            if (myShapeType == 18)
            {
                //multipoint Z (with m)
                fullLength = 20 + (numpoints * 8)  +8 +4*numpoints + 8 +4*numpoints;
            }
            else
            {
                //multipoint M (with M)
                fullLength = 20 + (numpoints * 8)  +8 +4*numpoints;
            }

            if (contentLength >= fullLength)  //is the M portion actually there?
            {
                file.readDoubleLE(); //m min/max
                file.readDoubleLE();
                actualReadWords += 8;
                for (int t=0;t<numpoints;t++)
                {
                    file.readDoubleLE();//m
                    actualReadWords += 4;
                }
            }
        }

        //verify that we have read everything we need
        while (actualReadWords < contentLength)
        {
            int junk2 = file.readShortBE();
            actualReadWords += 1;
        }

        return geometryFactory.createMultiPointFromCoords(coords);
    }

    double[] zMinMax(Geometry g)
    {
        double zmin,zmax;
        boolean validZFound = false;
        Coordinate[] cs = g.getCoordinates();
        double[] result = new double[2];

        zmin = Double.NaN;
        zmax = Double.NaN;
        double z;

        for (int t=0;t<cs.length; t++)
        {
            z= cs[t].getZ();
            if (!(Double.isNaN( z ) ))
            {
                if (validZFound)
                {
                    if (z < zmin)
                        zmin = z;
                    if (z > zmax)
                        zmax = z;
                }
                else
                {
                    validZFound = true;
                    zmin =  z ;
                    zmax =  z ;
                }
            }

        }

        result[0] = (zmin);
        result[1] = (zmax);
        return result;

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
        MultiPoint mp = (MultiPoint) geometry;

        if (myShapeType == 8)
            return mp.getNumGeometries() * 8 + 20;
        if (myShapeType == 28)
            return mp.getNumGeometries() * 8 + 20 +8 +4*mp.getNumGeometries();

        return mp.getNumGeometries() * 8 + 20 +8 +4*mp.getNumGeometries() + 8 +4*mp.getNumGeometries() ;
    }
}
