Api-X Image Services
===================================


### Image on Docker Hub
`pandorasystems/image-services`
-----------------
[![](https://images.microbadger.com/badges/image/pandorasystems/image-services.svg)](https://microbadger.com/images/pandorasystems/image-services "pandorasystems/image-services")[![](https://images.microbadger.com/badges/version/pandorasystems/image-services.svg)](https://microbadger.com/images/pandorasystems/image-services "pandorasystems/image-services")


This contains image serialization and encoding extensions for API-X.
These are OSGi services that extend the functionality of a [Fedora4](https://wiki.duraspace.org/display/FF/Fedora+Repository+Home) repository.

Extensions
----------
* [`exts-image`](exts-image): An image manipulation service
* [`exts-encoder`](exts-encoder): Serializes TIFF images as OpenJP2 to a file system directory.

Building
--------

Create OSGI bundles
```sh 
    $ gradle install
```
Copy bundles from local Maven repository to Docker Build directory
```sh      
    $ gradle copyTask
```
Build Docker image
```sh 
    $ gradle docker
```
Execute Docker Compose
 ```sh     
    $ docker-compose up
  ```   

More information
----------------
See [pandora-demo](https://github.com/pan-dora/pandora-demo) for implementation example.


