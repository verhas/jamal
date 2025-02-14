#!/bin/bash
#
# This script is used to automate the installation of the development version of jamal-asciidoc plugin
# from inside the development environment.
#
echo "Compiling and installing Jamal, the whole project"
if ! mvn clean install ;then
  exit 1
fi

VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Creating the JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
mkdir -p .asciidoctor/lib
echo "cd into JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
cd .asciidoctor/lib || exit 1
echo "removing all old files"
rm -f *
echo "unzipping the libraries from the local maven repo"
unzip ~/.m2/repository/com/javax0/jamal/jamal-asciidoc/$VERSION/jamal-asciidoc-$VERSION-jamal-asciidoc-distribution.zip >/dev/null
echo "cd back to Jamal main project directory"
cd ../.. || exit 1
echo "Restart IntelliJ, then you have the new version"
echo "DONE"
