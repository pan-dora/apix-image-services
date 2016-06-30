Amherst College Repository Services
===================================

[![Build Status](https://travis-ci.org/acoburn/repository-extension-services.png?branch=master)](https://travis-ci.org/acoburn/repository-extension-services)

This is a collection of OSGi services that extend the functionality of a [Fedora4](https://wiki.duraspace.org/display/FF/Fedora+Repository+Home) repository.

Extensions
----------

These modules extend the behavior of Fedora resources. Specifically, they connect Fedora Resources to specific services
by making available a REST-based HTTP interface. The intention is that these extensions will be compatible with the
[Fedora API Extension](https://wiki.duraspace.org/display/FF/Design+-+API+Extension+Architecture) architecture.

* `acrepo-exts-fits`: This service will return FITS information associated with a Fedora Binary, in XML format
* `acrepo-exts-image`: An image manipulation service
* `acrepo-exts-jsonld`: This module exposes an HTTP endpoint for creating compact JSON-LD documents from a Fedora repository using a pluggable context document
* `acrepo-exts-pcdm`: This constructs a complete PCDM object graph for Fedora resources
* `acrepo-exts-serialize-xml`: This service translates Fedora RDF documents into MODS/XML or DC/XML
* `acrepo-exts-template`: A module for converting Fedora resources into some other form, using a [mustache](https://mustache.github.io/) template.

Services
--------

These modules provide particular services, independent of Fedora Resources.

* `acrepo-services-jsonld`: This service creates expanded or compact JSON-LD representations of input documents
* `acrepo-services-ldcache`: This service dereferences and caches URIs, retrieving the `object` of particular triples on demand
* `acrepo-services-ldcache-file`: A file-based backend for the `acrepo-services-ldcache` service
* `acrepo-services-mint`: This mints random (public) URIs for use with Fedora resources
* `acrepo-services-pcdm`: This makes it easy to work with PCDM objects
* `acrepo-services-validation`: An OSGi-based validation service

Connectors
----------

These modules listen to repository events and react accordingly.

* `acrepo-connector-broadcast`: ActiveMQ Message Broadcast Service:  This rebroadcasts messages from one queue/topic to every queue/topic in a specified list
* `acrepo-connector-idiomatic`: Id Mapping Service: This maps a public ID to a (internal and typically much longer) Fedora URI
* `acrepo-connector-idiomatic-pgsql`: Id Mapping Service Database: This exposes a Postgres datastore for use with the Id Mapping service

Other OSGi Features
-------------------

In addition to what is listed above, a number of Karaf features are made available to make it easier to install
sets of related bundles in an OSGi container.

* `acrepo-libs-jackson`: The [Jackson](http://wiki.fasterxml.com/JacksonHome) JSON libraries
* `acrepo-libs-jena`: The [Jena 3.x](http://jena.apache.org/) libraries
* `acrepo-libs-jsonld`: The [JSON-LD](https://github.com/jsonld-java/jsonld-java) libraries
* `acrepo-libs-marmotta`: The [Marmotta](http://marmotta.apache.org) libraries
* `acrepo-libs-sesame`: The [Sesame 2.x](http://rdf4j.org/) libraries

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features

    feature:install acrepo-connector-broadcast
    feature:install acrepo-connector-idiomatic
    feature:install acrepo-connector-idiomatic-pgsql

    feature:install acrepo-exts-fits
    feature:install acrepo-exts-image
    feature:install acrepo-exts-jsonld
    feature:install acrepo-exts-pcdm
    feature:install acrepo-exts-serialize-xml
    feature:install acrepo-exts-template

    feature:install acrepo-services-jsonld
    feature:install acrepo-services-ldcache
    feature:install acrepo-services-ldcache-file
    feature:install acrepo-services-mint
    feature:install acrepo-services-pcdm
    feature:install acrepo-services-validation

More information
----------------

For more information, please visit https://acdc.amherst.edu or https://acdc.amherst.edu/wiki/

