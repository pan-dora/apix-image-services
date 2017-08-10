exts-encoder
=====================================

This service depends on [`exts-image`](../exts-image).  

ActiveMQ and Camel 
--------
Using Apache Camel, the service reads messages from the ActiveMQ fedora broker queue and checks for binary resource
creation events.  It filters events where the content-type matches "image/tiff" or "image/jp2".

ImageMagick and OpenJPEG
--------
It then gets these resources from the [`exts-image`](../exts-image) endpoint that uses [ImageMagick](https://github.com/ImageMagick/ImageMagick) convert compiled with [OpenJPEG](https://github.com/uclouvain/openjpeg)
to pipe the repository original source files as JP2 to a configured file system directory.

