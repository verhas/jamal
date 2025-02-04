#!/bin/sh
# DO NOT EDIT 

#!/bin/bash

if [ ! -f "../../jamal.sh" ]; then
echo "Error: ../../jamal.sh not found!"
exit 1
fi

chmod 0700 ../../jamal.sh
../../jamal.sh ../../jamal-maven-load/it.sh.jam ../../jamal-maven-load/it.sh
../../jamal.sh integrationtest.jam integrationtest
../../jamal.sh README.adoc.jam README.adoc


if [ ! -f "test_groovy.sh.jam.jam" ]; then
    echo "Error: test_groovy.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_groovy.sh.jam.jam test_groovy.sh.jam 
if [ ! -f "test_ruby.sh.jam.jam" ]; then
    echo "Error: test_ruby.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_ruby.sh.jam.jam test_ruby.sh.jam 
if [ ! -f "test_py.sh.jam.jam" ]; then
    echo "Error: test_py.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_py.sh.jam.jam test_py.sh.jam 
if [ ! -f "test_scriptbasic.sh.jam.jam" ]; then
    echo "Error: test_scriptbasic.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_scriptbasic.sh.jam.jam test_scriptbasic.sh.jam 

if [ ! -f "test_groovy.txt.jam.jam" ]; then
    echo "Error: test_groovy.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_groovy.txt.jam.jam test_groovy.txt.jam 
if [ ! -f "test_ruby.txt.jam.jam" ]; then
    echo "Error: test_ruby.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_ruby.txt.jam.jam test_ruby.txt.jam 
if [ ! -f "test_py.txt.jam.jam" ]; then
    echo "Error: test_py.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_py.txt.jam.jam test_py.txt.jam 
if [ ! -f "test_scriptbasic.txt.jam.jam" ]; then
    echo "Error: test_scriptbasic.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.sh -T7 test_scriptbasic.txt.jam.jam test_scriptbasic.txt.jam 


docker build -t jamal-test .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi
docker run -it jamal-test