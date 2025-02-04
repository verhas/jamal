FROM --platform=$BUILDPLATFORM alpine:latest
WORKDIR /home/jamal
RUN apk add --no-cache python3 py3-pip
COPY prebuild.sh /home/jamal/prebuild.sh
COPY build.sh /home/jamal/build.sh
COPY jamal /jamal
RUN sh /home/jamal/prebuild.sh
USER jamal
RUN sh /home/jamal/build.sh
CMD [ "/jamal" ]