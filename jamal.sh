#!/usr/bin/env bash
MODULES="api engine core tools cmd word snippet scriptbasic groovy ruby plantuml debug jamal yaml io assertions jamal"
REPO=$HOME/.m2/repository
REPO_JAMAL=$REPO/com/javax0/jamal
VERSION=1.12.2
CENTRAL=https://repo1.maven.org/maven2

for MODULE in $MODULES ; do
  if ! test -f $REPO_JAMAL/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar; then
    if command -v wget &>/dev/null; then
      wget --no-check-certificate $CENTRAL/com/javax0/jamal/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar -O $REPO_JAMAL/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
    else
      if command -v curl &>/dev/null; then
        curl $CENTRAL/com/javax0/jamal/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar -o $REPO_JAMAL/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
      else
        echo "There is no curl nor wget available"
        exit -1
      fi
    fi
  fi
done

for MODULE in $MODULES ; do
  CLASSPATH=$CLASSPATH:$REPO_JAMAL/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
done

java -cp $CLASSPATH javax0.jamal.cmd.JamalMain $*