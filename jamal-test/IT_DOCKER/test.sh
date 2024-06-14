#!/bin/sh
chmod 0700 ../../jamal.sh
../../jamal.sh ../../jamal-maven-load/it.sh.jam ../../jamal-maven-load/it.sh
../../jamal.sh integrationtest.jam integrationtest
../../jamal.sh README.adoc.jam README.adoc
docker build -t jamal-test .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi
docker run -it jamal-test