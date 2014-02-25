mongoFS
===========

An enhancement of MongoDB's GridFS to allow for more features and capabilities.

This project came out of a need in my company to extend the current GridFS
functionality to include compression of file data. After spending a day or so 
trying to work with the existing implementation, I determined that the existing 
implementation was not going to be extendible due to the current use of inner 
classes and heavy cross-class collusion. So I decided to re-write the existing 
implementation so I could to extend it.

This project is currently under development, stay tuned. I should have a usable
version available shortly. Check out my task list under the [TODO](TODO.md) to 
check the progress. I need to have this code into production by end of March so 
keep checking back to see the progress..

