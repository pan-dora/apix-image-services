#Amherst College ID Mapping Service

This service implements a way to map external IDs to internal fedora URIs.

##Building

To build this project use

    mvn install

##Running from the command line

To run the project you can execute the following Maven goal

    mvn camel:run

##Deploying in OSGi

This project can be deployed in an OSGi container. For example using
[Apache ServiceMix](http://servicemix.apache.org/) or
[Apache Karaf](http://karaf.apache.org), you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/repository-services/LATEST/xml/features
    feature:install acrepo-idmapper

Or by copying the compiled bundle into `$KARAF_HOME/deploy`.

##Configuration

The application can be configured by creating the following configuration
file `$KARAF_HOME/etc/edu.amherst.acdc.idmapper.cfg`. The following values
are available for configuration:

In the event of failure, the maximum number of times a redelivery will be attempted.

    error.maxRedeliveries=10

The connection URI used to connect to a local or remote ActiveMQ broker

    jms.brokerUrl=tcp://localhost:61616

The camel URI for the incoming message stream.

    input.stream=activemq:topic:fedora

The RDF property used to identify external IDs

    id.property=dc:identifier

The full namespace for the `id.property` property

    id.namespace=http://purl.org/dc/elements/1.1/

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the Apache Camel documentation

    http://camel.apache.org/

