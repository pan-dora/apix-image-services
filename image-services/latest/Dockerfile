FROM pandorasystems/karaf:4.0.9

MAINTAINER Christopher Johnson <chjohnson39@gmail.com>
LABEL description = "Provides a Karaf container configured with Image Service features"

ENV DEBUG_PORT 5008
ENV JAVA_DEBUG_PORT=${DEBUG_PORT} \
    KARAF_VERSION=LATEST \
    CAMEL_VERSION=2.19.2 \
    PANDORA_EXTS_VERSION=LATEST \
    FCREPO_HOST=fcrepo \
    FCREPO_PORT=8080 \
    FCREPO_CONTEXT_PATH=/fcrepo \
    OPENJPEG_VERSION=2.1.2 \
    MAVEN_REPO=/build/repository \
    ACTIVEMQ_HOST=activemq

ENV KARAF_RUNTIME /opt/karaf
ENV KARAF_VERSION 4.0.8

RUN mkdir -p ${MAVEN_REPO}
ADD repository/ ${MAVEN_REPO}

WORKDIR ${KARAF_RUNTIME}/apache-karaf-${KARAF_VERSION}

RUN echo "pandora=mvn:cool.pandora/imaging-karaf/${PANDORA_EXTS_VERSION}/xml/features" >> etc/org.apache.karaf.features.repos.cfg

RUN sed -e "s:^\(    mvn\:org.apache.karaf.features/enterprise/${KARAF_VERSION}/xml/features\):\1, \\\
    mvn\:cool.pandora/imaging-karaf/${PANDORA_EXTS_VERSION}/xml/features \\\:" \
        -i etc/org.apache.karaf.features.cfg

RUN \
  echo "eval 'alias mvn=\"mvn -Dmaven.repo.local=\${MAVEN_REPO}\"'" \
    >> /etc/skel/.bashrc && \
  echo "eval 'alias mvn=\"mvn -Dmaven.repo.local=\${MAVEN_REPO}\"'" \
    >> ~/.bashrc

RUN bin/start && \
    bin/client -r 10 -d 5  "feature:repo-add camel ${CAMEL_VERSION}" && \
    bin/client -r 10 -d 5  "feature:repo-add activemq LATEST" && \
    bin/client -r 10 -d 5  "feature:repo-add mvn:org.fcrepo.camel/toolbox-features/${TOOLBOX_VERSION}/xml/features" && \
    bin/client -r 10 -d 5  "feature:repo-add mvn:cool.pandora/imaging-karaf/LATEST/xml/features" && \
    bin/client -r 10 -d 5  "feature:install exts-image" && \
    bin/client -r 10 -d 5  "feature:install exts-encoder" && \
    sleep 15 && \
    bin/stop

RUN sed -e "s/osgi:/stdout, osgi:/" -i etc/org.ops4j.pax.logging.cfg

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

#RUN wget -O kakadu.zip http://kakadusoftware.com/wp-content/uploads/2014/06/KDU79_Demo_Apps_for_Linux-x86-64_170108.zip \
#&& unzip kakadu.zip && mkdir -p ${KAKADU_HOME} && mv KDU*/* ${KAKADU_HOME} && rm kakadu.zip

WORKDIR /tmp/image-magick
RUN git clone https://github.com/ImageMagick/ImageMagick.git ./
RUN ./configure --prefix=/usr --with-modules --with-perl=/usr/bin/perl --with-jp2 --enable-shared --disable-static && \
make && make install

WORKDIR ${KARAF_RUNTIME}/apache-karaf-${KARAF_VERSION}
COPY repository/cool/pandora/*.cfg etc/
COPY repository/cool/pandora/entrypoint.sh /entrypoint.sh

RUN chmod 700 /entrypoint.sh

EXPOSE 9105

ENTRYPOINT [ "/entrypoint.sh" ]

# Launch Karaf with no console, and debugging enabled by default.

CMD [ "server" ]
