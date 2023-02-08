# gis
A simple Java library for loading ESRI Shapefiles.

## Dependencies
Java Topology Suite:
- org.locationtech.jts.io
- org.locationtech.jts-core

## Example
```
    InputStream mainInputStream = getMainInputStream();
    Shapefile shapefile = new Shapefile(mainInputStream);

    // Read the entire geometry collection from the shapefile.
    GeometryCollection geometryCollection = shapefile.read(new GeometryFactory());
    mainInputStream.close();
```

