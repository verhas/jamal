#!/usr/bin/env bash
#
# This shell script was automatically compiled from install-asciidoc.jam
#
mvn clean install
mkdir -p ../.asciidoctor/lib
pushd ../.asciidoctor/lib
rm -f *
unzip ~/.m2/repository/com/javax0/jamal/jamal-asciidoc/1.12.3/jamal-asciidoc-1.12.3-jamal-asciidoc-distribution.zip >/dev/null
popd
