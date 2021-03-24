#!/usr/bin/env bash
MODULES="api engine core tools cmd"
REPO=$HOME/.m2/repository/com/javax0/jamal
VERSION=1.7.3-SNAPSHOT

for MODULE in $MODULES ; do
  if ! test -f $REPO/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar; then
    if command -v wget &>/dev/null; then
      wget --no-check-certificate https://repo1.maven.org/maven2/com/javax0/jamal/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar -O $REPO/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
    else
      if command -v curl &>/dev/null; then
        curl https://repo1.maven.org/maven2/com/javax0/jamal/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar -o $REPO/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
      else
        echo "There is no curl nor wget available"
        exit -1
      fi
    fi
  fi
done

CLASSPATH=""
for MODULE in $MODULES ; do
  CLASSPATH=$REPO/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar:$CLASSPATH
done

java -cp $CLASSPATH javax0.jamal.cmd.JamalMain $*

