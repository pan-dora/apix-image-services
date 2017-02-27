Repository Inference Service
============================

This OSGi service can do structural typing of RDF graphs using OWL restrictions.
It does _not_ do validation!

Building
--------

To build this project use

    gradle install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache Karaf](http://karaf.apache.org) version 4.x or better, you can run the following
command from its shell:

    feature:repo-add mvn:cool.pandora/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-services-inference

