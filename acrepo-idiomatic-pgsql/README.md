Amherst College ID Mapping Service (PostgreSQL)
===============================================

This service implements a database backend for the ID Mapping Service

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache ServiceMix](http://servicemix.apache.org/) or
[Apache Karaf](http://karaf.apache.org), you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-idmapper-pgsql

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

Configuration
-------------

The application can be configured by creating or editing the following configuration
file `$KARAF_HOME/etc/org.ops4j.datasource-idiomatic.cfg`. The following values
are available for configuration:

The name of the database

    databaseName=idiomatic

The hostname for the database server

    serverName=localhost

The port for the database server

    portNumber=5432

The username for connecting to the database

    user=

The password for connecting to the database

    password=

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the Apache Camel documentation

    http://camel.apache.org/

