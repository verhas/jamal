FROM alpine
WORKDIR /home/jamal
COPY prebuild.sh /home/jamal/prebuild.sh
COPY build.sh /home/jamal/build.sh
COPY jamal /jamal
RUN sh /home/jamal/prebuild.sh
USER jamal
RUN sh /home/jamal/build.sh
CMD [ "/jamal" ]