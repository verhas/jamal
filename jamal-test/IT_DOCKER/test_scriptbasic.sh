#!/bin/sh
export JAMAL=../../jamal.sh

echo "maven.load.include=com.javax0.jamal:jamal-test:*,com.javax0.jamal:jamal-scriptbasic:1.12.6
maven.load.exclude=.com.javax0.jamal:jamal-api:*" > ~/.jamal/settings.properties

chmod 0700 $JAMAL
chmod 0500 ~/.jamal
chmod 0400 ~/.jamal/settings.properties

$JAMAL test_scriptbasic.txt.jam test_scriptbasic.test

chmod 0700 ~/.jamal
chmod 0700 ~/.jamal/settings.properties
rm ~/.jamal/settings.properties

if [ "$(diff -b test_scriptbasic.txt test_scriptbasic.test)" ] ;then
  echo "Jamal via SHELL created different output"
  diff -b test_scriptbasic.txt test_scriptbasic.test
  exit 1
fi
rm test_scriptbasic.test
