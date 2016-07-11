Amherst College ID Mapping Service
==================================

This service implements a way to map external IDs to internal fedora URIs.

Database Structure
------------------

The backing database is assumed to be accessed via SQL. It follows a very simple
format, using a single table (`uris`) with two columns (`public` and `fedora`):

    CREATE TABLE uris (
        public VARCHAR(1024) CONSTRAINT publicid PRIMARY KEY,
        fedora VARCHAR(1024) CONSTRAINT fedoraid NOT NULL);

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
    feature:install acrepo-connector-idiomatic

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.connector.idiomatic.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The camel URI for the incoming message stream.

    input.stream=broker:topic:fedora

The RDF property used to identify external IDs

    id.property=dc:identifier

The full namespace for the `id.property` property

    id.namespace=http://purl.org/dc/elements/1.1/

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

