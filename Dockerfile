FROM alpine
RUN apk update
RUN apk add --no-cache openjdk17
RUN apk add --no-cache maven
RUN apk add --no-cache graphviz
RUN apk add --no-cache git
RUN addgroup TESTGROUP
RUN adduser -G TESTGROUP -D -s /bin/bash jamal

WORKDIR /home/jamal
COPY integrationtest .
RUN chown jamal:TESTGROUP integrationtest
RUN chmod u+xr integrationtest
USER jamal
RUN mkdir -p /home/jamal/.m2/repository
RUN chmod u+xr /home/jamal/.m2/repository
CMD pwd ; ls -l ; ./integrationtest