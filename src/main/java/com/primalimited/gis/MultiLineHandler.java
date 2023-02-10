package com.primalimited.gis;

import com.mapbox.geojson.Feature;
import org.locationtech.jts.geom.*;

import java.io.IOException;
import java.util.function.Consumer;

class MultiLineHandler implements ShapeHandler {
    int myShapeType = -1;
    private PrecisionModel precisionModel = new PrecisionModel();
    private GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 0);

    MultiLineHandler()
    {
        myShapeType = 3;
    }

    MultiLineHandler(int type) throws InvalidShapefileException
    {
        if  ( (type != 3) &&  (type != 13) &&  (type != 23) )
            throw new InvalidShapefileException("MultiLineHandler constructor - expected type to be 3,13 or 23");

        myShapeType = type;
    }

    @Override
    public void streamFeature(EndianDataInputStream file, int recordIndex, int contentLength, Consumer<Feature> consumer) throws IOException, InvalidShapefileException {
        //TODO
        throw new IllegalStateException("not implemented");
    }

    @Override
    public void stream(EndianDataInputStream file, GeometryFactory geometryFactory, int recordIndex, int contentLength, Consumer<Geometry> consumer) throws IOException, InvalidShapefileException {
        double junk;
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        //file.setLittleEndianMode(true);

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType ==0) {
            consumer.accept(geometryFactory.createMultiLineString(null)); //null shape
        }

        if (shapeType != myShapeType) {
            throw new InvalidShapefileException("MultilineHandler.read()  - file says its type "+shapeType+" but i'm expecting type "+myShapeType);
        }

        //read bounding box (not needed)
        junk = file.readDoubleLE();
        junk =file.readDoubleLE();
        junk =file.readDoubleLE();
        junk =file.readDoubleLE();
        actualReadWords += 4*4;

        int numParts = file.readIntLE();
        int numPoints = file.readIntLE();//total number of points
        actualReadWords += 4;

        int[] partOffsets = new int[numParts];

        for ( int i = 0; i < numParts; i++ ){
            partOffsets[i]=file.readIntLE();
            actualReadWords += 2;
        }

        LineString lines[] = new LineString[numParts];
        Coordinate[] coords = new Coordinate[numPoints];

        for (int t =0;t<numPoints; t++) {
            coords[t] = PrecisionModelLatLong.INSTANCE.createCoordinate(file.readDoubleLE(),file.readDoubleLE());
            actualReadWords += 8;
        }

        if (myShapeType ==13) {
            junk =file.readDoubleLE();  //z min, max
            junk =file.readDoubleLE();
            actualReadWords += 8;

            for (int t =0;t<numPoints; t++) {
                coords[t].setZ(PrecisionModelLatLong.INSTANCE.makePrecise(file.readDoubleLE())); //z value
                actualReadWords += 4;
            }
        }

        if (myShapeType >=13) {
            //  int fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints+ 4+4+4*numPoints;
            int fullLength;
            if (myShapeType == 13) {
                //polylineZ (with M)
                fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints+ 4+4+4*numPoints;
            } else {
                //	polylineM (with M)
                fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints;
            }

            if (contentLength >= fullLength) //are ms actually there?
            {
                junk =file.readDoubleLE();  //m min, max
                junk =file.readDoubleLE();
                actualReadWords += 8;

                for (int t =0;t<numPoints; t++)
                {
                    junk =file.readDoubleLE(); //m value
                    actualReadWords += 4;
                }
            }
        }

        //verify that we have read everything we need
        while (actualReadWords < contentLength) {
            int junk2 = file.readShortBE();
            actualReadWords += 1;
        }

        int offset = 0;
        int start,finish,length;
        for(int part=0;part<numParts;part++){
            start = partOffsets[part];
            if(part == numParts-1)
            {
                finish = numPoints;
            }
            else {
                finish=partOffsets[part+1];
            }
            length = finish-start;
            Coordinate points[] = new Coordinate[length];
            for(int i=0;i<length;i++){
                points[i]=coords[offset];
                offset++;
            }
            lines[part] = geometryFactory.createLineString(points);
        }

        Geometry geometry = numParts == 1 ?
                lines[0] : geometryFactory.createMultiLineString(lines);
        geometry.setUserData(recordIndex);
        consumer.accept(geometry);
    }

    @Override
    public Geometry read(EndianDataInputStream file , GeometryFactory geometryFactory, int contentLength) throws IOException,InvalidShapefileException {
        double junk;
        int actualReadWords = 0; //actual number of words read (word = 16bits)

        //file.setLittleEndianMode(true);

        int shapeType = file.readIntLE();
        actualReadWords += 2;

        if (shapeType ==0) {
            return geometryFactory.createMultiLineString(null); //null shape
        }

        if (shapeType != myShapeType) {
            throw new InvalidShapefileException("MultilineHandler.read()  - file says its type "+shapeType+" but i'm expecting type "+myShapeType);
        }

        //read bounding box (not needed)
        junk = file.readDoubleLE();
        junk =file.readDoubleLE();
        junk =file.readDoubleLE();
        junk =file.readDoubleLE();
        actualReadWords += 4*4;


        int numParts = file.readIntLE();
        int numPoints = file.readIntLE();//total number of points
        actualReadWords += 4;

        int[] partOffsets = new int[numParts];

        for ( int i = 0; i < numParts; i++ ){
            partOffsets[i]=file.readIntLE();
            actualReadWords += 2;
        }

        LineString lines[] = new LineString[numParts];
        Coordinate[] coords = new Coordinate[numPoints];

        for (int t =0;t<numPoints; t++) {
            coords[t] = PrecisionModelLatLong.INSTANCE.createCoordinate(file.readDoubleLE(),file.readDoubleLE());
            actualReadWords += 8;
        }

        if (myShapeType ==13) {
            junk =file.readDoubleLE();  //z min, max
            junk =file.readDoubleLE();
            actualReadWords += 8;

            for (int t =0;t<numPoints; t++) {
                coords[t].setZ(PrecisionModelLatLong.INSTANCE.makePrecise(file.readDoubleLE())); //z value
                actualReadWords += 4;
            }
        }

        if (myShapeType >=13) {
            //  int fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints+ 4+4+4*numPoints;
            int fullLength;
            if (myShapeType == 13) {
                //polylineZ (with M)
                fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints+ 4+4+4*numPoints;
            } else {
                //	polylineM (with M)
                fullLength =  22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints;
            }

            if (contentLength >= fullLength) //are ms actually there?
            {
                junk =file.readDoubleLE();  //m min, max
                junk =file.readDoubleLE();
                actualReadWords += 8;

                for (int t =0;t<numPoints; t++)
                {
                    junk =file.readDoubleLE(); //m value
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


        int offset = 0;
        int start,finish,length;
        for(int part=0;part<numParts;part++){
            start = partOffsets[part];
            if(part == numParts-1)
            {
                finish = numPoints;
            }
            else {
                finish=partOffsets[part+1];
            }
            length = finish-start;
            Coordinate points[] = new Coordinate[length];
            for(int i=0;i<length;i++){
                points[i]=coords[offset];
                offset++;
            }
            lines[part] = geometryFactory.createLineString(points);

        }
        if (numParts ==1)
            return lines[0];
        else
            return geometryFactory.createMultiLineString(lines);
    }

    /**
     * Get the type of shape stored (Shapefile.ARC)
     */
    @Override
    public int getShapeType(){
        return myShapeType;
    }

    @Override
    public int getLength(Geometry geometry){
        MultiLineString multi = (MultiLineString) geometry;

        int numlines, numpoints;

        numlines = multi.getNumGeometries();
        numpoints = multi.getNumPoints();

        if (myShapeType == 3)
        {
            return 22 + 2*numlines + (numpoints * 8);
        }
        if (myShapeType == 23)
        {
            return 22 + 2*numlines + (numpoints * 8) + 4+4+4*numpoints;
        }


        return 22 + 2*numlines + (numpoints * 8) + 4+4+4*numpoints+ 4+4+4*numpoints;


        //   return 22 + 2*numlines + (numpoints * 8);

        //return (44+(4*((GeometryCollection)geometry).getNumGeometries()));
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
            z= cs[t].getZ() ;
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
}
