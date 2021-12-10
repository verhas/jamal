#!/usr/bin/env bash
MODULES="api engine core tools cmd"
REPO=$HOME/.m2/repository
REPO_JAMAL=$REPO/com/javax0/jamal
VERSION=1.10.2
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

PICOCLI_VERSION=4.6.1
PICOCLI=info/picocli/picocli/$PICOCLI_VERSION/picocli-$PICOCLI_VERSION.jar

if ! test -f $REPO/$PICOCLI; then
  if command -v wget &>/dev/null; then
    wget --no-check-certificate $CENTRAL/$PICOCLI -O $REPO/$PICOCLI
  else
    if command -v curl &>/dev/null; then
      curl $CENTRAL/$PICOCLI -o $REPO/$PICOCLI
    else
      echo "There is no curl nor wget available"
      exit -1
    fi
  fi
fi

CLASSPATH=$REPO/$PICOCLI
for MODULE in $MODULES ; do
  CLASSPATH=$CLASSPATH:$REPO_JAMAL/jamal-$MODULE/$VERSION/jamal-$MODULE-$VERSION.jar
done

java -cp $CLASSPATH javax0.jamal.cmd.JamalMain $*