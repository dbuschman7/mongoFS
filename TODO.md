#Version Road Map
##0.1 Intial baseline
* create git repo
* pull over mongo-java-driver code for gridFS - with units tests
* make them run as is 

##0.2 Refactor out the output and inputstream objects
* refactor out OutputStream and unit test it
* Pull internal OutputStream object into parent class

## 0.3 Create chuck outputstream objects
* Use outputStreams to handle filling the buffers correctly 
* refactor GridFSDBInputFile and GridFS
* add chunking buffer to simplify buffer handling

## 0.4 Optimize the inputStream reading files side 
* streamline file reading
* fixed chunking buffering to always use the chunking buffer

##0.5 MongoFile and mongoFS base classes##
* create mongofile URL and handler to enable external referencing
* create MongoFSDataStore to front the DBCollections
* create MongoFile class to hold the state 
* Add in CompressedOutputStream object with tests
* Add in CompressedInputSteam object with tests
* Add in mongofile:gz protocol to handle regular and gzip storage

##0.6 Finish compression support##
* enable find* methods, handle DBCursors and pagination
* enable toggling on compression on a per file basis
* add configuration support to toggle feature
* verify mongoFS find* on top of pre-existing GridFS collections.
* create initial github pages for project

##0.7 Temporary File Storage
* Add temporary files generator with TTL collections
* Allow file to live only for a finite time, minutes, days, weeks.
* finish out github pages project

## Futures ideas
* auto-grow chunk size as "empty-space" entropy rises, maybe multiple collections 
* migrate to NIO for better streaming performance
* archival strategy for saving off files.
* full zip file support with searchable manifests
