Repository PCDM Object Handler
==============================

This module makes it easier to work with PCDM resources.
It uses simple OWL inference to handle inverse properties.

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
    feature:install acrepo-services-pcdm


