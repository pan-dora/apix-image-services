Amherst College ID Mapping Service (MySQL)
===============================================

This service implements a database backend for the ID Mapping Service

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
    feature:install acrepo-connector-idiomatic-mysql

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

    portNumber=3306

The username for connecting to the database

    user=

The password for connecting to the database

    password=

By editing this file, this service will be immediately redeployed with the new values.

For more help see the [Apache Camel](http://camel.apache.org/) documentation


