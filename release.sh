#!/usr/bin/env bash

echo Cleaning old release directory if any
rm -rf release
echo Creating directory 01engine
mkdir -p release/01engine
cp jamal-engine/target/*.jar release/01engine
cp jamal-engine/pom.xml release/01engine

echo Creating directory 02api
mkdir -p release/02api
cp jamal-api/target/*.jar release/02api
cp jamal-api/pom.xml release/02api

echo Creating directory 03tools
mkdir -p release/03tools
cp jamal-tools/target/*.jar release/03tools
cp jamal-tools/pom.xml release/03tools

echo Creating directory 04core
mkdir -p release/04core
cp jamal-core/target/*.jar release/04core
cp jamal-core/pom.xml release/04core

echo Creating directory 05extensions
mkdir -p release/05extensions
cp jamal-extensions/target/*.jar release/05extensions
cp jamal-extensions/pom.xml release/05extensions

echo Creating directory 06maven
mkdir -p release/06maven
cp jamal-maven-plugin/target/*.jar release/06maven
cp jamal-maven-plugin/pom.xml release/06maven

echo Creating directory 07testsupport
mkdir -p release/07testsupport
cp jamal-testsupport/target/*.jar release/07testsupport
cp jamal-testsupport/pom.xml release/07testsupport

echo Creating directory 08cmd
mkdir -p release/08cmd
cp jamal-cmd/target/*.jar release/08cmd
cp jamal-cmd/pom.xml release/08cmd

cd release
for artifact in *
do
    for file in ${artifact}/*.jar ${artifact}/pom.xml
    do
        echo Signing ${file}
        gpg -s -b ${file}
        mv ${file}.sig ${file}.asc
    done
    cd ${artifact}
    echo Creating ${artifact}_release.zip
    jar -c -M -f ${artifact}_release.zip *.jar pom.xml *.asc
    cd ..
done
echo Creating parent bundle
cp ../pom.xml .
gpg -s -b pom.xml
mv pom.xml.sig pom.xml.asc
jar -c -M -f 00parent_release.zip  pom.xml pom.xml.asc
cd ..
echo done.
