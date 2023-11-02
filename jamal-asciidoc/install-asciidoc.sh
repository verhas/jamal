#!/usr/bin/env bash
#
# This shell script was automatically compiled from install-asciidoc.sh.jam
# It is used to automate the installation of the development version of jamal-asciidoc plugin
# from inside the development environment.
#
echo "Compiling and installing the Jamal asciidoctor plugin only"
mvn clean install
echo "Creating the JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
mkdir -p ../.asciidoctor/lib
echo "cd into JAMAL_PROJECT_HOME/.asciidoctor/lib directory"
pushd ../.asciidoctor/lib
echo "removing all old files"
rm -f *
echo "unzipping the libraries from the local maven repo"
unzip ~/.m2/repository/com/javax0/jamal/jamal-asciidoc/2.5.0-SNAPSHOT/jamal-asciidoc-2.5.0-SNAPSHOT-jamal-asciidoc-distribution.zip >/dev/null
echo "cd back to jamal-asciidoc project directory"
popd
echo "Restart IntelliJ, then you have the new version"
echo "DONE"
