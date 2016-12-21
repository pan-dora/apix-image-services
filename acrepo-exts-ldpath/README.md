Repository LDPath service
=========================

This is an extension of the `fcrepo-ldpath` service found in [`fcrepo-camel-toolbox`](https://github.com/fcrepo4-exts/fcrepo-camel-toolbox).

The interface and configuration is the same as with [`fcrepo-ldpath`](https://github.com/fcrepo4-exts/fcrepo-camel-toolbox/tree/master/fcrepo-ldpath).

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

This projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-exts-ldpath

Configuration
-------------

## Configuration

The application can be configured by creating a file in
`$KARAF_HOME/etc/edu.amherst.acdc.exts.ldpath.cfg`. The following
values are available for configuration:

If the fedora repository requires authentication, the following values
can be set:

    fcrepo.authUsername=<username>
    fcrepo.authPassword=<password>
    fcrepo.authHostname=localhost
    fcrepo.authPort=8080

The baseUrl for the fedora repository.

    fcrepo.baseUrl=http://localhost:8080/fcrepo/rest

The time Fedora triples are cached (in seconds)

    fcrepo.cache.timeout=0

The global timeout for cache entries (in seconds)

    cache.timeout=86400

The host to which to bind the HTTP endpoint

    rest.host=localhost

The port at which ldpath requests can be sent.

    rest.port=9086

The URL path prefix for the ldpath service.

    rest.prefix=/ldpath

By editing this file, any currently running routes will be immediately redeployed
with the new values.

For more help see the Apache Camel documentation

    http://camel.apache.org/
