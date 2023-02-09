# gis
A simple Java library for loading ESRI Shapefiles.

## Dependencies
Java Topology Suite:
- org.locationtech.jts.io
- org.locationtech.jts-core

## Basics
Shapefiles consist of a minimum of 2 files:
- ._shp_ the main file, containing the feature data.
- ._shx_ the index file, with offsets and lengths into the main file for each record.  

An optional database file:
- ._dbf_ the dBASE file, basically a table with any number of columns of metadata for each record in the main file.  

An optional project file:
- ._prj_ the Project file, contains map projection info.

## Examples
- Load geometries from a shapefile:
```
    InputStream mainInputStream = getMainInputStream(); // Enter your own implementation here!
    Shapefile shapefile = new Shapefile(mainInputStream);

    // Read the entire geometry collection from the shapefile.
    GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
    mainInputStream.close();
```
- Read a specific record from a shapefile, including metadata:
```
    // User clicks on a polyline on the map, returning the unique index into the geometry collection.
    int selectedRecordIndex = getSelectedRecordIndex();

    // Select the specific geometry from the record index.
    Geometry geometry = geometryCollection.getGeometryN(selectedRecordIndex);

    // First, we need to read the header info from the .dbf file.
    InputStream headerStream = getDbfInputStream(); // Enter your own implementation here!
    DBASEReader reader = new DBASEReader(headerStream);
    DBASEReader.DBASEHeaderInfo header = reader.readHeader();
    headerStream.close();

    // Now read the record from the .dbf file.
    InputStream dbfStream = getDbfInputStream(); // Enter your own implementation here!
    DBASEReader reader = new DBASEReader(dbfStream);
    List<DBField> fields = reader
        .readRecord(
            dbfStream, 
            header, 
            selectedRecordIndex
        );

    dbfStream.close();
```
