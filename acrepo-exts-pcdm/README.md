Repository PCDM object extension
================================

This extension operates on `pcdm:Object` resources, building
an RDF graph of the complete object (following `pcdm:hasMember`,
`pcdm:hasRelatedObject` and `pcdm:hasFile` links). The complete
graph is returned in the requested serialization, using an `Accept` header.

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

This projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:cool.pandora/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-exts-pcdm
    feature:install acrepo-services-pcdm

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/cool.pandora.exts.pcdm.cfg`. The following values
are available for configuration:

The base url of the fedora repository

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

The port on which the service is made availalbe

    rest.port=9107

The hostname for the service

    rest.host=localhost

The REST prefix

    rest.prefix=/pcdm

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

