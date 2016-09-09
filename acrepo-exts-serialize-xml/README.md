Amherst College XML-based metadata serialization extension
=========================================================

This service implements a translation service from Fedora RDF-based metadata
to either a DC/XML or MODS/XML serialization. This translation relies on
pluggable XSLT 2.0 documents.

The service becomes available over HTTP on the configured port. For example,
in order to retrieve a MODS version of the resource `a/b/c`:

    curl localhost:9104/mods/a/b/c

And the DC version:

    curl localhost:9104/dc/a/b/c

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-exts-serialize-xml

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.exts.serialize.xml.cfg`. The following values
are available for configuration:

The location of the XSLT document for MODS. This can be a file path (using the `file:` prefix)
or an external URL (e.g. using a `http:` scheme). Without a prefix, the XSL file will
be loaded from the classpath.

    mods.xslt=edu/amherst/acdc/xml/metadata/rdf2mods.xsl

The location of the XSLT document for DC. This can be a file path (using the `file:` prefix)
or an external URL (e.g. using a `http:` scheme). Without a prefix, the XSL file will
be loaded from the classpath.

    dc.xslt=edu/amherst/acdc/xml/metadata/rdf2dc.xsl

The port on which the service is available

    rest.port=9104

The hostname for the service

    rest.host=localhost

The fedora baseUrl value

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

