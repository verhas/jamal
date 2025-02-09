#!/bin/sh

echo "
testproperty=Peter Verhas' macbook
graphviz=/usr/local/bin/dot
#asciidoc.extension.log=false
asciidoc.extension.external.command=/Users/verhasp/.jbang/bin/jbang jamal@verhas -open=\{% -close=%\} $1 $2
maven.load.repo=https://repo1.maven.org/maven2/
maven.load.local=.m2/repository
maven.load.include=com.javax0.jamal:jamal-test:*
maven.load.exclude=.com.javax0.jamal:jamal-api:*" >~/.jamal/settings.properties

echo "chmod 0777 settings.properties"
chmod 0500 ~/.jamal
chmod 0777 ~/.jamal/settings.properties
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
    echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0777 ~/.jamal/settings.properties"
    exit 1
fi

echo "chmod 0707 settings.properties"
chmod 0500 ~/.jamal
chmod 0707 ~/.jamal/settings.properties
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
    echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0707 ~/.jamal/settings.properties"
    exit 1
fi

echo "chmod 0770 settings.properties"
chmod 0500 ~/.jamal
chmod 0770 ~/.jamal/settings.properties
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
    echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0770 ~/.jamal/settings.properties"
    exit 1
fi

echo "chmod 0700 settings.properties"
chmod 0500 ~/.jamal
chmod 0700 ~/.jamal/settings.properties
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
    echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0700 ~/.jamal/settings.properties"
    exit 1
fi

echo "chmod 0600 settings.properties"
chmod 0500 ~/.jamal
chmod 0600 ~/.jamal/settings.properties
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationSecure
if [ "$?" -ne "0" ]; then
    echo "TEST Integration_ConfigurationSecure FAILED with chmod 0600 ~/.jamal/settings.properties"
    exit 1
fi

echo "chmod 0777 ~/.jamal"
chmod 0777 ~/.jamal
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0777 ~/.jamal"
exit 1
fi
echo "chmod 0707 ~/.jamal"
chmod 0707 ~/.jamal
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0707 ~/.jamal"
exit 1
fi
echo "chmod 0770 ~/.jamal"
chmod 0770 ~/.jamal
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationNotSecure
if [ "$?" -ne "0" ]; then
echo "TEST Integration_ConfigurationNotSecure FAILED with chmod 0770 ~/.jamal"
exit 1
fi
echo "chmod 0700 ~/.jamal"
chmod 0700 ~/.jamal
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationSecure
if [ "$?" -ne "0" ]; then
echo "TEST Integration_ConfigurationSecure FAILED with chmod 0700 ~/.jamal"
exit 1
fi
echo "chmod 0500 ~/.jamal"
chmod 0500 ~/.jamal
mvn test -Dtest=javax0.jamal.maven.load.Integration_ConfigurationSecure
if [ "$?" -ne "0" ]; then
echo "TEST Integration_ConfigurationSecure FAILED with chmod 0500 ~/.jamal"
exit 1
fi