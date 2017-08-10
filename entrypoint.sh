#!/bin/bash

# Update the Karaf configuration so that the Maven repository under ${MAVEN_REPO}
# is used by Karaf to discover features.

sed -e "s:^org.ops4j.pax.url.mvn.localRepository=.*:org.ops4j.pax.url.mvn.localRepository=${MAVEN_REPO}:" \
        -i etc/org.ops4j.pax.url.mvn.cfg

# Change "fcrepo.baseUrl=localhost:8080/fcrepo/rest" to "fcrepo.baseUrl=fcrepo:${FCREPO_PORT}/fcrepo/rest"
for f in `ls etc/cool.pandora.*` ;
do
  sed -e "s:localhost\:8080/fcrepo/rest:${FCREPO_HOST}\:${FCREPO_PORT}${FCREPO_CONTEXT_PATH}/rest:" -i $f
done

echo "#empty" > /etc/hosts

# Execute `bin/karaf` with any arguments suppled by CMD
exec bin/karaf "$@"