#!/bin/bash

cd "$(dirname "$(readlink -f "$0")")"

# Read the first line containing <version>...</version> from pom.xml
VERSION=$(grep -m1 '<version>.*</version>' ../pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/')
TVERSION=$(echo $VERSION | sed '/-SNAPSHOT$/s/.*/1.0.0/')

# Export the VERSION variable
export VERSION

echo "Version=$VERSION"

mkdir -p target/JARS
rm -rf target/JARS/*
unzip target/jamal-cmd-${VERSION}-distribution.zip -d target/JARS

# Function to create package based on the operating system
create_package() {
    local INSTALLER_TYPE=$1
    jpackage --input target/JARS \
        --name jamal \
        --app-version ${TVERSION} \
        --main-jar jamal-cmd-${VERSION}.jar \
        --main-class javax0.jamal.cmd.JamalMain \
        --type $INSTALLER_TYPE \
        --dest output \
        --java-options -Xmx2048m \
        --resource-dir src/packaging-resources
}

# Detect the operating system and create appropriate package
case "$(uname -s)" in
    Linux*)
        create_package deb
        ;;
    Darwin*)
        create_package pkg
        ;;
    *)
        echo "Unsupported operating system"
        exit 1
        ;;
esac
