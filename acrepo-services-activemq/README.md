Repository ActiveMQ OSGi handling service
========================================

This OSGi service creates a connector to an ActiveMQ Broker. It may be used by other
modules in the repository service to connect up to an ActiveMQ Broker.

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-services-activemq

Configuration
-------------

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.services.activemq.cfg`. The following values
are available for configuration:

The url for connecting to the ActiveMQ broker

    jms.brokerUrl=tcp://localhost:61616

If the ActiveMQ broker requires authentication, these properties will be useful:

    jms.username=<username>
    jms.password=<password>

Once the file is edited this service will be automatically redeployed
with the new configuration.

For more help see the [Apache Camel](http://camel.apache.org/) documentation

