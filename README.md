Amherst College Repository Services
===================================

[![Build Status](https://travis-ci.org/acoburn/repository-extension-services.png?branch=master)](https://travis-ci.org/acoburn/repository-extension-services)

This is a collection of OSGi services that extend the functionality of a Fedora4 repository.

Extensions
----------

These modules extend the behavior of Fedora resources. Specifically, they connect Fedora Resources to specific services
by making available a REST-based HTTP interface.

* `acrepo-fits-rest`: This service will return FITS information associated with a Fedora Binary, in XML format
* `acrepo-exts-jsonld`: This module exposes an HTTP endpoint for creating compact JSON-LD documents from a fedora repository using a pluggable context document
* `acrepo-image-service`: An image manipulation service
* `acrepo-xml-metadata`: This service translates Fedora RDF documents into MODS/XML or DC/XML

Services
--------

These modules provide particular services, independent of Fedora Resources.

* `acrepo-services-jsonld`: This service creates expanded or compact JSON-LD representations of input documents
* `acrepo-services-ldcache`: This service dereferences and caches URIs, retrieving the `object` of particular triples on demand
* `acrepo-services-mint`: This mints random (public) URIs for use with fedora resources
* `acrepo-services-validation`: An OSGi-based validation service

Connectors
----------

These modules listen to repository events and react accordingly.

* `acrepo-idiomatic`: Id Mapping Service: This maps a public ID to a (internal and typically much longer) fedora URI
* `acrepo-idiomatic-pgsql`: Id Mapping Service Database: This exposes a Postgres datastore for use with the Id Mapping service

Other OSGi Features
-------------------

In addition to what is listed above, a number of Karaf features are made available to make it easier to install
sets of related bundles in an OSGi container.

* `acrepo-libs-jena`: The Jena 3.x libraries
* `acrepo-libs-marmotta`: The Marmotta libraries
* `acrepo-libs-sesame`: The Sesame 2.x libraries
* `acrepo-libs-jackson`: The Jackson JSON libraries
* `acrepo-libs-jsonld`: The JSON-LD libraries

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
    feature:install acrepo-fits-rest
    feature:install acrepo-idiomatic
    feature:install acrepo-idiomatic-pgsql
    feature:install acrepo-image-service
    feature:install acrepo-exts-jsonld
    feature:install acrepo-services-mint
    feature:install acrepo-services-jsonld
    feature:install acrepo-services-ldcache
    feature:install acrepo-services-mint
    feature:install acrepo-services-validation
    feature:install acrepo-xml-metadata

More information
----------------

For more information, please visit https://acdc.amherst.edu or https://acdc.amherst.edu/wiki/

