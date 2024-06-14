#!/bin/bash
set -e


# Variables
PACKAGE_NAME="jamal"
VERSION=$(grep /version ../../pom.xml |head -1| sed "s/ *<[/]*version> *//g")
REPO=build/repo
ARCHITECTURE="all"
MAINTAINER="Peter Verhas <peter@verhas.com>"
DESCRIPTION="Jamal - Meta-Markup language"
DEPENDENCIES="openjdk-17-jdk"
MAIN_CLASS=javax0.jamal.cmd.JamalMain

rm -rf build
mkdir build

# Create directories
mkdir -p ${REPO}/DEBIAN
mkdir -p ${REPO}/usr/local/bin

# Create the control file
cat <<EOL > "${REPO}/DEBIAN/control"
Package: ${PACKAGE_NAME}
Version: ${VERSION}
Architecture: ${ARCHITECTURE}
Maintainer: ${MAINTAINER}
Depends: ${DEPENDENCIES}
Description: ${DESCRIPTION}
EOL


# Copy the JAR files
rm -rf  ${REPO}/usr/local/bin/jamal.d
mkdir  ${REPO}/usr/local/bin/jamal.d
unzip -d ${REPO}/usr/local/bin/jamal.d "../../jamal-cmd/target/jamal-cmd-${VERSION}-distribution.zip"
cd ${REPO}
CLASSPATH=$(find usr/local/bin/jamal.d -type f | sed 's#^#/#' | paste -sd ':' -)
cd ..
cd ..

cat <<EOL > "${REPO}/usr/local/bin/jamal"
#!/bin/bash
exec java -cp ${CLASSPATH} ${MAIN_CLASS} "\$@"
EOL

# Make the script executable
chmod +x ${REPO}/usr/local/bin/jamal

cat <<EOL > build/build_deb.sh
#!/bin/bash
# Build the package
dpkg-deb --build repo

# Run lintian to check for common issues
lintian ${PACKAGE_NAME}-${VERSION}.deb

# Move the package to the output directory
mv ${PACKAGE_NAME}-${VERSION}.deb /build
EOL

chmod +x build/build_deb.sh


# create the container to build the package
docker build -t jamal-builder:latest .
docker run -d -v "$(pwd)/build":/build jamal-builder:latest

