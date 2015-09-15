#Amherst College Repository Services

This is a collection of services that extend the functionality of a Fedora4 repository.

##Services

* Id Mapping Service: This maps a public ID to a (internal and much longer) fedora URI
* Id Mapping Service Database: This exposes a Postgres datastore for use with the Id Mapping service
* Minter: This mints random (public) URIs for use with fedora resources

##Building

To build this project use

    mvn install

##Deploying in OSGi

Each of these projects can be deployed in an OSGi container. For example using
[Apache ServiceMix](http://servicemix.apache.org/) or
[Apache Karaf](http://karaf.apache.org), you can run the following
command from its shell:

    feature:repo-add mvn:edu.amherst.acdc/acrepo-karaf/LATEST/xml/features
    feature:install acrepo-idmapper
    feature:install acrepo-idmapper-pgsql
    feature:install acrepo-mint

Or by copying any of the compiled bundles into `$KARAF_HOME/deploy`.

##More information

For more information, please visit https://acdc.amherst.edu or https://acdc.amherst.edu/wiki/

