Linked Data Cache Service
=========================

This module provides access to a linked data caching service. A backend implementation
must also be installed (such as `acrepo-services-ldcache-file`).

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-services-ldcache
    feature:install acrepo-services-ldcache-file

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.services.ldcache.cfg`. The following values
are available for configuration:

Set the number of seconds before items expire (0=default value: 1 day)

    ldcache.timeout=0

By editing this file, this service will be immediately redeployed
with the new configuration.

For more help see the [Apache Camel](http://camel.apache.org/) documentation

