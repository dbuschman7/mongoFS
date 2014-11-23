mongoFS
===========

An enhanced file storage implementation in MongoDB that extends the GridFS functionality of the Mongo-Java-Driver

#Features
* Optimized for usePowerOf2Sizes storage supported in MongoDB 2.4+
* Background deletes ( asynchronous )     
* Temporary file storage ( expiration )       
* Provides URL syntax for easy file reference     
* Space saving data compression ( gzip )      
* Storing files with customer provided encryption      
* 3.0.x driver compatible objects using the 2.11-13.x drivers 
* ZIP archive expansion and manifests     

#Roadmap 
* Java 3.0 driver compatible - Coming ( 1.0.x )
* Non-blocking IO and reactive - Coming ( 1.1.x )

#Summary
This project came out of a need in my company to extend the current GridFS functionality to include compression of file data. After spending a day or so trying to work with the existing implementation, I determined that the existing implementation was not going to be extendible due to the current use of inner classes and heavy cross-class collusion. So I decided to re-write the existing implementation in order to extend it and publish those enhancements for anyone else who will like to make use of it.

Also the "coming soon" Mongo-Java-Driver 3.0.x which is an almost complete re-write by the MonogDB folks has a new and much cleaner API and Object Model, I need a way to bridge the gap seamlessly at my current company because we are making heavy use of the library using the 2.10.x thru 2.12.x drivers. I need to be able to migrate these systems to the new 3.0.x driver when it is released.

To see comparision of MongoFS to the old GridFS implementation, click [here](http://dbuschman7.github.io/mongoFS/#feature "Features") 

#Usage
Goto the [downloads](http://dbuschman7.github.io/mongoFS/#download "Download")  section to see the latest version and how to get it.

To get started, checkout the [Usage](https://github.com/dbuschman7/mongoFS/blob/master/usage.md "Usage")  page on Github

Check out my task list on the [TODO](https://github.com/dbuschman7/mongoFS/blob/master/TODO.md " TODO") page to check the progress.

Check out the [Usage](usage.md) page on how to use the library.
