Amherst College Repository Services
===================================

This is a collection of services that extend the functionality of a Fedora4 repository.

Services
--------

* `acrepo-idiomatic`: Id Mapping Service: This maps a public ID to a (internal and typically much longer) fedora URI
* `acrepo-idiomatic-pgsql`: Id Mapping Service Database: This exposes a Postgres datastore for use with the Id Mapping service
* `acrepo-mint-service`: This mints random (public) URIs for use with fedora resources
* `acrepo-mods-xml-service`: This service translates Fedora RDF documents into MODS/XML

Building
--------

To build this project use

    mvn install

Deploying in OSGi
-----------------

Each of these projects can be deployed in an OSGi container. For example using
[Apache ServiceMix](http://servicemix.apache.org/) or
[Apache Karaf](http://karaf.apache.org), you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-idiomatic
    feature:install acrepo-idiomatic-pgsql
    feature:install acrepo-mint-service
    feature:install acrepo-mods-xml-service

Or by copying any of the compiled bundles into `$KARAF_HOME/deploy`.


More information
----------------

For more information, please visit https://acdc.amherst.edu or https://acdc.amherst.edu/wiki/

