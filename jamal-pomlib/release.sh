#!/usr/bin/env bash

echo Cleaning old release directory if any
rm -rf release
echo Creating directory release
mkdir release

cp target/*.jar release
cp pom.xml release

cd release

for file in *.jar pom.xml
do
    echo Signing ${file}
    gpg -s -b ${file}
    mv ${file}.sig ${file}.asc
done
echo Creating release.zip
jar -c -M -f release.zip *.jar pom.xml *.asc

cd ..

echo done.
