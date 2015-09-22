Amherst College Template Rendering Service
==========================================

This service renders fedora resources using a Mustache template.

The service becomes available over HTTP on the configured port. For example,
in order to retrieve a MODS version of the resource `a/b/c`:

    curl localhost:9070/template/a/b/c

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
    feature:install acrepo-template-mustache

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.template.mustache.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The port on which the service is available

    rest.port=13433

The fedora baseUrl value

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

The location of the mustache template

    mustache.template=edu/amherst/acdc/template/mustache/template.mustache

The content-type of the rendered template

    mustache.contentType=text/html

The json+ld context file

    jsonld.context=https://acdc.amherst.edu/jsonld/context.json

The riak caching datastore host

    riak.host=localhost:8098

The riak caching datastore bucket name

    riak.bucket=fcrepo

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

