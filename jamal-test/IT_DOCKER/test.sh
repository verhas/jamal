#!/bin/sh
chmod 0700 ../../jamal.sh
../../jamal.sh ../../jamal-maven-load/it.sh.jam ../../jamal-maven-load/it.sh
../../jamal.sh integrationtest.jam integrationtest
../../jamal.sh README.md.jam README.md
docker build -t jamal-test .
docker run -it jamal-test