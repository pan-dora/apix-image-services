Amherst College Triplestore Indexer
===================================

The Triplestore indexer will index Fedora content into an external triplestore. It differs
from the `fcrepo-indexer-triplestore` in that each resource is indexed into its own named
graph.

`edu.amherst.acdc.connector.triplestore.cfg` is the configuration file for this service.

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install fcrepo-service-activemq
    feature:install acrepo-connector-triplestore

Configuration
-------------

This application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.connector.triplestore.cfg`. The following
values are available for configuration:

The Camel URI for the incoming message stream

    input.stream=broker:queue:fedora

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

It is possible to control the representation of fedora resources with Prefer headers
by including or excluding certain types of triples. For instance, `ldp:contains` triples
are excluded by default. This is so because, for large repositories, the `ldp:contains` triples
may number in the hundreds of thousands or millions of triples, which lead to very large
request/response sizes. It is important to note that `fedora:hasParent` functions as a logical
inverse of `ldp:contains`, so in the context of a triplestore, you can use the inverse
property in SPARQL queries to much the same effect. Alternately, a built-in reasoner will
allow you to work directly with `ldp:contains` triples even if they haven't been explicitly
added to the triplestore.

    prefer.omit=http://www.w3.org/ns/ldp#PreferContainment
    prefer.include=

The camel URI for handling reindexing events.

    triplestore.reindex.stream=broker:queue:triplestore.reindex

The base URL of the triplestore being used.

    triplestore.baseUrl=http://localhost:8080/fuseki/test/update

The Fedora configuration.

    fcrepo.baseUrl=http://localhost:8080/fcrepo/rest
    fcrepo.authUsername=
    fcrepo.authPassword=

A comma-delimited list of URIs to filter. That is, any Fedora resource that either matches or is contained in one of
the URIs listed will not be processed by the application.

    filter.containers=http://localhost:8080/fcrepo/rest/test

By editing this file, any currently running routes in this service will be immediately redeployed
with the new values.

More information
----------------

For more information, please visit [Apache Camel](http://camel.apache.org) documentation
