#!/usr/bin/env bash
mvn clean install
pushd ../.asciidoctor/lib
rm -f *
unzip ~/.m2/repository/com/javax0/jamal/jamal-asciidoc/1.12.3-SNAPSHOT/jamal-asciidoc-1.12.3-SNAPSHOT-jamal-asciidoc-distribution.zip >/dev/null
popd
