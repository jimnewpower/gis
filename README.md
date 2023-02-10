# gis
A simple Java library for loading ESRI Shapefiles.

## Dependencies
[Java Topology Suite](https://github.com/locationtech/jts):
- [org.locationtech.jts-core](https://mvnrepository.com/artifact/org.locationtech.jts/jts-core)

## Documentation
[ESRI Shapefile Technical Description](./ShapefileTechnicalDescription.pdf)

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
Geometry geometry = getClickedData(); // Enter your own implementation here!

// User data should have been set during read (as the record index from zero, including zero).
UserData userData = new UserData(geometry.getUserData());
int recordIndex = userData.toInt();

// Now read the record from the .dbf file.
InputStream dbfStream = getDbfInputStream(); // Enter your own implementation here!
DBASEReader reader = new DBASEReader();
List<DBField> fields = reader.readRecord(
    dbfStream, 
    recordIndex
);

dbfStream.close();
```
- Stream geometries asynchronously:
```
Callable<GeometryCollection> readTask = () -> {
    InputStream mainFileInputStream = getInputStream(); // Enter your own implementation here!
    Shapefile shapefile = new Shapefile(mainFileInputStream);
    List<Geometry> geometries = new ArrayList<>();
    shapefile.stream(new GeometryFactory(), (geometry) -> geometries.add(geometry));
    mainFileInputStream.close();
    return new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[geometries.size()]));
};

ExecutorService executorService = Executors.newFixedThreadPool(1);
Future<GeometryCollection> future = executorService.submit(readLines);

while (!future.isDone()) {
    Thread.sleep(50/*milliseconds*/);
}
GeometryCollection geometryCollection = future.get();
```