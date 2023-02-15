
FROM alpine
RUN apk update
RUN apk add --no-cache openjdk17 maven graphviz git fontconfig ttf-droid
RUN addgroup TESTGROUP
RUN adduser -G TESTGROUP -D -s /bin/bash jamal

WORKDIR /home/jamal
USER jamal
CMD /bin/sh