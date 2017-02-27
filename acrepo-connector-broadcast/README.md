Amherst College Message Broadcaster
===================================

The Message Broadcast service will take messages off one ActiveMQ queue or topic and broadcast the
messages across any number of other queues specified in the config file.

`cool.pandora.connector.broadcast.cfg` is the configuration file for this service.

Deploying in OSGi
-----------------

This project can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x and above, you can run the following
command from its shell:

    feature:repo-add mvn:cool.pandora/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-connector-broadcast

Configuration
-------------

This application can be configured by creating the following configuration
file `$KARAF_HOME/etc/cool.pandora.connector.broadcast.cfg`. The following
values are available for configuration:

The Camel URI for the incoming message stream

    input.stream=broker:topic:fedora

Comma separate list of recipient queues to broadcast the incoming messages to

    message.recipients=broker:queue:fcrepo-serialization,broker:queue:fcrepo-indexing-triplestore

By editing this file, any currently running routes in this service will be immediately redeployed
with the new values.

More information
----------------

For more information, please visit [Apache Camel](http://camel.apache.org) documentation
