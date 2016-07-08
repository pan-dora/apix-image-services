Repository FITS Service
===================================

This service will return the FITS information associated with a Fedora Binary, in
XML format.  The service can be used with any Camel route in an OSGi container.

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-exts-fits


Configuration
-------------
The application can be configured by creating the following configuration
file `KARAF_HOME/etc/edu.amherst.acdc.exts.fits.cfg`. The following values 
are available for configuration:

The base URL of the Fedora repository and any authentication parameters

    fcrepo.baseUrl=localhost:8080/fcrepo/rest
    fcrepo.authHost=
    fcrepo.authUsername=
    fcrepo.password=

The prefix for the service

    rest.prefix=/fits

The port on which the service is made available

    rest.port=9601

The hostname on which the service is available

    rest.host=localhost

The endpoint on which the FITS server is located

    fits.endpoint=localhost:8080/fits

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation
