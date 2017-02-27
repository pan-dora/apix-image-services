Amherst College Template Rendering Service
==========================================

This service renders fedora resources using a Mustache template.

The service becomes available over HTTP on the configured port. For example,
in order to retrieve a MODS version of the resource `a/b/c`:

    curl localhost:9103/template/a/b/c

One can have the module use a customized Mustache template by doing the following:

    curl localhost:9103/template/a/b/c?templateUri=/path/to/template/template2.mustache

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:cool.pandora/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-exts-template

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/cool.pandora.exts.template.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The port on which the service is available

    rest.port=9103

The hostname for the service

    rest.host=localhost

The prefix for the service

    rest.prefix=/template

The fedora baseUrl value

    fcrepo.baseUrl=localhost:8080/fcrepo/rest

The location of the mustache template

    mustache.templateUri=edu/amherst/acdc/exts/template/mustache/template.mustache

The content-type of the rendered template

    mustache.contentType=text/html

The json+ld context file

    jsonld.context=https://acdc.amherst.edu/jsonld/context.json

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the [Apache Camel](http://camel.apache.org) documentation

