Amherst College MODS/XML transformation service
===============================================

This service implements a translation service from Fedora RDF-based metadata
to a MODS/XML serialization. This translation relies on a pluggable XSLT
document.

Building
--------

To build this project use

    mvn install

To run the project you can execute the following Maven goal

    mvn camel:run

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache ServiceMix](http://servicemix.apache.org/) or
[Apache Karaf](http://karaf.apache.org), you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-mods-xml-service

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.mods.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The location of the XSLT document. This can be a file path (using the `file:` prefix)
or an external URL (e.g. using a `http:` scheme). Without a prefix, the XSL file will
be loaded from the classpath.

    mods.xslt=edu/amherst/acdc/mods/rdf2mods.xsl

The port on which the service is available

    rest.port=9070

The URL prefix for the service

    rest.prefix=/mods

The fedora baseUrl value

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

