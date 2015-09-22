Amherst College XML-based metadata transformation service
=========================================================

This service implements a translation service from Fedora RDF-based metadata
to either a DC/XML or MODS/XML serialization. This translation relies on
pluggable XSLT 2.0 documents.

The service becomes available over HTTP on the configured port. For example,
in order to retrieve a MODS version of the resource `a/b/c`:

    curl localhost:9070/mods/a/b/c

And the DC version:

    curl localhost:9070/dc/a/b/c

Building
--------

To build this project use

    mvn install

To run the project you can execute the following Maven goal

    mvn camel:run

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-xml-metadata

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.xml.metadata.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The location of the XSLT document for MODS. This can be a file path (using the `file:` prefix)
or an external URL (e.g. using a `http:` scheme). Without a prefix, the XSL file will
be loaded from the classpath.

    mods.xslt=edu/amherst/acdc/xml/metadata/rdf2mods.xsl

The location of the XSLT document for DC. This can be a file path (using the `file:` prefix)
or an external URL (e.g. using a `http:` scheme). Without a prefix, the XSL file will
be loaded from the classpath.

    dc.xslt=edu/amherst/acdc/xml/metadata/rdf2dc.xsl

The port on which the service is available

    rest.port=9070

The fedora baseUrl value

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

