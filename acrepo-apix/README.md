API Extension Framework for Fedora
==================================

This implements the API-X Framework for Fedora.

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-apix

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.apix.cfg`. The following values
are available for configuration:

The REST endpoint prefix.

    rest.prefix=/apix

The port on which the API-X service is available.

    rest.port=13431

The service prefix

    apix.prefix=svc:

The fedora base URL

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

The fedora username

    fcrepo.authUsername=

The fedora password

    fcrepo.authPassword=

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

