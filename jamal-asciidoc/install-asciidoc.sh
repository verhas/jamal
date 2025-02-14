#!/usr/bin/env bash
echo "Compiling and installing the Jamal asciidoctor plugin only"
if ! mvn clean install ;then
  exit 1
fi

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Creating the JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
mkdir -p ../.asciidoctor/lib

echo "cd into JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
cd ../.asciidoctor/lib || exit 1
echo "removing all old files"
rm -f *
echo "unzipping the libraries from the local maven repo"
unzip ~/.m2/repository/com/javax0/jamal/jamal-asciidoc/$VERSION/jamal-asciidoc-$VERSION-jamal-asciidoc-distribution.zip >/dev/null
echo "cd back to jamal-asciidoc project directory"
cd ../../jamal-asciidoc || exit 1
echo "Restart IntelliJ, then you have the new version"
echo "DONE"