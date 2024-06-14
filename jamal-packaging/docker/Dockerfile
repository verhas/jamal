# Dockerfile
FROM debian:latest

RUN apt-get update
RUN apt-get install -y openjdk-17-jdk

RUN mkdir -p /build
RUN mkdir -p /lib

WORKDIR /workspace

# Download the JAR file and other necessary files
ARG VERSION
COPY build/lib/* /build/lib/
COPY jamal /build/jamal
RUN chmod +x /build/jamal

ENTRYPOINT ["/build/jamal"]
