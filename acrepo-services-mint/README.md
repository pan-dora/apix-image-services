Repository ID Minting Service
=============================

This module mints random IDs for use with a repository. The service is used with
the `acrepo-idiomatic` service, but it can be replaced with any service that
implements the same interface, using `osgi.jndi.service.name=acrepo/Minter`.

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
    feature:install acrepo-services-mint

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.services.mint.cfg`. The following values
are available for configuration:

Set the length of the newly minted ID field

    minter.length=7

By editing this file, this service will be immediately redeployed
with the new configuration.

For more help see the [Apache Camel](http://camel.apache.org/) documentation

