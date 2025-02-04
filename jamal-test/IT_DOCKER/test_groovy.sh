#!/bin/sh

export JAMAL=../../jamal.sh

echo "maven.load.include=com.javax0.jamal:jamal-test:*,com.javax0.jamal:jamal-groovy:2.8.3-SNAPSHOT
maven.load.exclude=.com.javax0.jamal:jamal-api:*" > ~/.jamal/settings.properties

chmod 0700 $JAMAL
chmod 0500 ~/.jamal
chmod 0400 ~/.jamal/settings.properties

$JAMAL test_groovy.txt.jam test_groovy.test

chmod 0700 ~/.jamal
chmod 0700 ~/.jamal/settings.properties
rm ~/.jamal/settings.properties

if [ "$(diff -b test_groovy.txt test_groovy.test)" ] ;then
  echo "Jamal via SHELL created different output"
  diff -b test_groovy.txt test_groovy.test
  exit 1
fi
rm test_groovy.test