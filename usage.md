usage
=======
This library that  different approach to storing files and is more Java centric that GridFS is in the mongo-java-driver. I made the decision to prefer static compile time type-checking and simplified object APIs over what is currently available from GridFS(2.11.4). The read, writing, and query functions are separated from the main store code to keep usage more simple for the most common read and writes. 


### URL 
A mongofile URL protocol has also been implemented to allow a single string to to represent all the info need to fetch the file back. Several examples look like this 

```
mongofile:/home/myself/foo/filename.zip?52fb1e7b36707d6d13ebfda9#application/zip
mongofile:gz:fileName.pdf?52fb1e7b36707d6d13ebfda9#application/pdf
``` 

### Stand up a MongoFileStore

Configure the connection to the MongoDB server and database in whatever fashion is available to you. Consult the MongoClient class from the driver for more Info.
 
THe database object below is a traditional com.mongodb.DB object.
```Java
String bucket = "myBucketName";
MongoFileStoreConfig config = new MongoFileStoreConfig(bucket);
config.setChunkSize(chunkSize);
config.setWriteConcern(WriteConcern.SAFE);

MongoFileStore store = new MongoFileStore(database, config);
```
Keep this object handy, it is the core of all operations with file stored in MongoFS. If you are using Spring, make it a bean and inject this object where you need to acces files.

###Writing files into the store

```Java
MongoFileWriter writer = store.createNew("README.md", "text/plain", true);
writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));
MongoFile file = writer.getMongoFile();
URL url = file.getURL();

System.out.println(url);
```
would print the following to stdout

```
mongofile:gz:README.md?52fb1e7b36707d6d13ebfda9#text/plain
```
where 
| Value | Meaning |
|-:|-|
| mongofile | the protocol |
| gz | compression was used to save space storing the file |
| README.md | the file path and name |
| 52fb1e7b36707d6d13ebfda9 | a UUID id generated by the MongoDB driver for this object |
| text/plain | the media type for the data |

Store the url string how you like and use it to fetch the file back from the store when its needed.


### Finding files from the store
Using a stored URL string 

```Java
MongoFileUrl url = MongoFileUrl.construct("mongofile:gz:README.md?52fb1e7b36707d6d13ebfda9#text/plain");
MongoFile mongoFile = store.getFile(url); // lookup the file by its url
  
ByteArrayOutputStream out = new ByteArrayOutputStream(32 * 1024);
store.read(mongoFile, out, true); // true == flush output stream when done

String fileText = out.toString();       
```

You can still read files by file name from the store as well.

```Java
MongoFileQuery query = store.query();
MongoFileCursor cursor =  query.find("/foo/bar1.txt");
int count = 0;
for (MongoFile mongoFile : cursor) {
    ++count;
    assertNotNull(mongoFile.getURL());
    assertEquals("/foo/bar1.txt", mongoFile.getFilename());
}
assertEquals(2, count);
```

