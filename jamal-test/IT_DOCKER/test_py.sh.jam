#!/bin/sh
export JAMAL=../../jamal.sh


chmod 0700 $JAMAL
chmod 0500 ~/.jamal

$JAMAL test_py.txt.jam test_py.test

chmod 0700 ~/.jamal

if [ "$(diff -b test_py.txt test_py.test)" ] ;then
    echo "Jamal via SHELL created different output"
    diff -b test_py.txt test_py.test
    exit 1
fi
rm test_py.test