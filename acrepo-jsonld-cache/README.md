Repository JSON-LD compaction service
=====================================

This collection of camel routes exposes an HTTP endpoint for
generating compact JSON-LD serializations of Fedora resources.

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

This projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-jsonld-cache

Or by copying any of the compiled bundles into `$KARAF_HOME/deploy`.

