Amherst College Repository Services
===================================

[![Build Status](https://travis-ci.org/acoburn/repository-extension-services.png?branch=master)](https://travis-ci.org/acoburn/repository-extension-services)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/edu.amherst.acdc/acrepo-karaf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/edu.amherst.acdc/acrepo-karaf/)

This is a collection of OSGi services that extend the functionality of a [Fedora4](https://wiki.duraspace.org/display/FF/Fedora+Repository+Home) repository.

Extensions
----------

These modules extend the behavior of Fedora resources. Specifically, they connect Fedora Resources to specific services
by making available a REST-based HTTP interface. The intention is that these extensions will be compatible with the
[Fedora API Extension](https://wiki.duraspace.org/display/FF/Design+-+API+Extension+Architecture) architecture.

* [`acrepo-exts-fits`](acrepo-exts-fits): This service will return FITS information associated with a Fedora Binary, in XML format
* [`acrepo-exts-image`](acrepo-exts-image): An image manipulation service
* [`acrepo-exts-jsonld`](acrepo-exts-jsonld): This module exposes an HTTP endpoint for creating compact JSON-LD documents from a Fedora repository using a pluggable context document
* [`acrepo-exts-pcdm`](acrepo-exts-pcdm): This constructs a complete PCDM object graph for Fedora resources
* [`acrepo-exts-serialize-xml`](acrepo-exts-serialize-xml): This service translates Fedora RDF documents into MODS/XML or DC/XML
* [`acrepo-exts-template`](acrepo-exts-template): A module for converting Fedora resources into some other form, using a [mustache](https://mustache.github.io/) template.

Services
--------

These modules provide particular services, independent of Fedora Resources.

* [`acrepo-services-activemq`](acrepo-services-activemq): This service creates a ActiveMQ connection for other modules to use
* [`acrepo-services-inference`](acrepo-services-inference): An OSGi-based structural typing service using owl inference
* [`acrepo-services-jsonld`](acrepo-services-jsonld): This service creates expanded or compact JSON-LD representations of input documents
* [`acrepo-services-mint`](acrepo-services-mint): This mints random (public) URIs for use with Fedora resources
* [`acrepo-services-pcdm`](acrepo-services-pcdm): This makes it easy to work with PCDM objects

Connectors
----------

These modules listen to repository events and react accordingly.

* [`acrepo-connector-broadcast`](acrepo-connector-broadcast): ActiveMQ Message Broadcast Service:  This rebroadcasts messages from one queue/topic to every queue/topic in a specified list
* [`acrepo-connector-idiomatic`](acrepo-connector-idiomatic): Id Mapping Service: This maps a public ID to a (internal and typically much longer) Fedora URI
* [`acrepo-connector-idiomatic-mysql`](acrepo-connector-idiomatic-mysql): Id Mapping Service Database: This exposes a MySQL datastore for use with the Id Mapping service
* [`acrepo-connector-idiomatic-pgsql`](acrepo-connector-idiomatic-pgsql): Id Mapping Service Database: This exposes a Postgres datastore for use with the Id Mapping service

Other OSGi Features
-------------------

In addition to what is listed above, a number of Karaf features are made available to make it easier to install
sets of related bundles in an OSGi container.

* `acrepo-libs-jackson`: The [Jackson](http://wiki.fasterxml.com/JacksonHome) JSON libraries
* `acrepo-libs-jena`: The [Jena 3.x](http://jena.apache.org/) libraries
* `acrepo-libs-jsonld`: The [JSON-LD](https://github.com/jsonld-java/jsonld-java) libraries

Building
--------

To build, test and install this project use either

    gradle check install

or (in the top level directory):

    ./gradlew check install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features

    feature:install acrepo-connector-broadcast
    feature:install acrepo-connector-idiomatic
    feature:install acrepo-connector-idiomatic-mysql
    feature:install acrepo-connector-idiomatic-pgsql

    feature:install acrepo-exts-fits
    feature:install acrepo-exts-image
    feature:install acrepo-exts-jsonld
    feature:install acrepo-exts-pcdm
    feature:install acrepo-exts-serialize-xml
    feature:install acrepo-exts-template

    feature:install acrepo-services-inference
    feature:install acrepo-services-jsonld
    feature:install acrepo-services-mint
    feature:install acrepo-services-pcdm

More information
----------------

For more information, please visit https://acdc.amherst.edu or https://acdc.amherst.edu/wiki/

