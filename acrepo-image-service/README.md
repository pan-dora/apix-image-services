Repository Image manipulation service
=====================================

This comprises an image manipulation service used in combination with ImageMagick's `convert` utility.
This service assumes that ImageMagick is installed somewhere.

The endpoint location is configurable. Any path appended to that endpoint root will be used
as a path to a Fedora Resource. The `options` parameter can be used to add output parameters
to ImageMagick. The `Accept` header is used to change the format of the image. By default,
the output is JPEG, but any format listed in the configuration under `valid.formats` can be used.
By default, the valid output formats are `image/tiff`, `image/jp2` and `image/jpeg`.

An example:

    curl http://localhost:9081/image/path/to/binary?options=-resize%20400x400 -H"Accept: image/jpeg"

The service is defined to operate on any `fedora:Binary` resources with a MIMEType of `image/*`.

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

This projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-image-service

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.image.service.cfg`. The following values
are available for configuration:

The base url of the fedora repository and any authentication parameters

    fcrepo.baseUrl=localhost:8080/fcrepo/rest
    fcrepo.authUsername=
    fcrepo.authPassword=

The acceptable output formats for images

    valid.formats=jpeg,jp2,tiff

The path to the `convert` utility

    convert.path=convert

The port on which the service is made availalbe

    rest.port=9081

The hostname on which the service is available

    rest.host=localhost

The prefix for the service

    rest.prefix=/image

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

