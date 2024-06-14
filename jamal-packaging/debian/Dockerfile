# Dockerfile
FROM debian:latest

RUN apt-get update && \
    apt-get install -y \
    wget \
    openjdk-17-jdk \
    dpkg-dev \
    dh-make \
    debhelper \
    devscripts

WORKDIR /build

ENTRYPOINT ["/build/build_deb.sh"]
