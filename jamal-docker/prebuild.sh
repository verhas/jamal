#! /bin/bash

#
# Commands executed before the build as root
#

apk update
apk add --no-cache openjdk17 maven graphviz git fontconfig ttf-droid bash
addgroup JAMAL
adduser -G JAMAL -D -s /bin/bash jamal
chown jamal /home/jamal
chown jamal /jamal
chmod u+rx /jamal

# create the empty repo, this is where we will create the application
mkdir -p /home/jamal/.m2/repository
chown jamal /home/jamal/.m2/repository
chmod u+wxr /home/jamal/.m2/repository

# we do not need the prebuild script anymore
rm /home/jamal/prebuild.sh
echo "READY TO BUILD"