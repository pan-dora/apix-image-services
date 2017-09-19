FROM pandorasystems/karaf:${KARAF_VERSION}

MAINTAINER Christopher Johnson <christopher_hanna.johnson@uni-leipzig.de>
LABEL description = "Provides a Karaf container configured with Image Service features"

ENV FCREPO_CAMEL_VERSION=${fcrepoCamelVersion}
ENV CAMEL_VERSION=${camelVersion}
ENV OPENJPEG_VERSION=${openJpegVersion}
ENV KARAF_VERSION=${KARAF_VERSION}

ENV DEBUG_PORT=${DEBUG_PORT}
ENV JAVA_DEBUG_PORT=${DEBUG_PORT}
ENV FCREPO_HOST=${FCREPO_HOST}
ENV FCREPO_PORT=${FCREPO_PORT}
ENV FCREPO_CONTEXT_PATH=${FCREPO_CONTEXT_PATH}

ENV REST_HOST=${REST_HOST}
ENV REST_PORT=${REST_PORT}
ENV REST_PREFIX=${REST_PREFIX}

ENV MAVEN_REPO=/build/repository
ENV EXTENSION_LOAD_URI=${EXTENSION_LOAD_URI}
ENV ACTIVEMQ_HOST=${ACTIVEMQ_HOST}
ENV KARAF_RUNTIME=${KARAF_RUNTIME}

RUN mkdir -p ${mavenRepo}
ADD repository/ ${mavenRepo}

RUN echo "pandora=mvn:cool.pandora.exts/imaging-karaf/${version}/xml/features" >> etc/org.apache.karaf.features.repos.cfg

RUN bin/start && \
    bin/client -r 10 -d 5  "feature:repo-add activemq ${activemqVersion}" && \
    bin/client -r 10 -d 5  "feature:repo-add mvn:org.fcrepo.camel/toolbox-features/${fcrepoCamelToolboxVersion}/xml/features" && \
    bin/client -r 10 -d 5  "feature:repo-add camel ${camelVersion}" && \
    bin/client -r 10 -d 5  "feature:install camel" && \
    bin/client -r 10 -d 5  "feature:install fcrepo-service-camel" && \
    bin/client -r 10 -d 5  "feature:repo-add mvn:cool.pandora.exts/imaging-karaf/${version}/xml/features" && \
    bin/client -r 10 -d 5  "feature:install exts-image" && \
    bin/client -r 10 -d 5  "feature:install exts-encoder" && \
    sleep 15 && \
    bin/stop

# Needed for -image
RUN apt-get update && \
    apt-get -y install \
    build-essential autoconf libfcgi0ldbl libtool libtiff5-dev libpng-dev libmemcached-dev \
    memcached liblcms2-2 liblcms2-dev libgomp1 libpthread-stubs0-dev liblzma5 \
    liblzma-dev libjbig-dev libjbig0 libz80ex1 libz80ex-dev pkg-config zlib1g-dev libopenjp2-7 bash \
    git cmake libltdl-dev libperl-dev && \
    rm -rf /var/lib/apt/lists/* && apt-get purge -y

### Download and compile openjpeg2 (master)
WORKDIR /tmp/openjpeg
RUN git clone https://github.com/uclouvain/openjpeg.git ./
RUN cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=/usr && make && make install

WORKDIR /tmp/image-magick
RUN git clone https://github.com/ImageMagick/ImageMagick.git ./
RUN ./configure --prefix=/usr --with-modules --with-perl=/usr/bin/perl --with-jp2 --enable-shared --disable-static && \
make && make install

WORKDIR ${KARAF_RUNTIME}/apache-karaf-${KARAF_VERSION}
COPY repository/cool/pandora/exts/*.cfg etc/
COPY repository/cool/pandora/exts/entrypoint.sh /entrypoint.sh

RUN chmod 700 /entrypoint.sh

EXPOSE 9105

ENTRYPOINT [ "/entrypoint.sh" ]

CMD [ "server" ]
