#! /bin/bash

#
# Commands executed to build Jamal executed as user jamal
#

# clone the source into the 'build' directory
git clone https://github.com/verhas/jamal.git  build

# We do not need to compile and run the tests here, this is not development, it is deployment.
# After this command Jamal JARs and dependencies will be in the local Maven repository and nothing else.
cd /home/jamal/build || exit 1
mvn -Dmaven.test.skip=true install
cd ..

# we do not need the source any more, all the JAR files are in the local repo
rm -rf build

# we do not need the build script anymore
rm /home/jamal/build.sh
echo "DONE"