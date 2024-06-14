#! /usr/bin/env bash

VERSION=$(grep /version ../../pom.xml |head -1| sed "s/ *<[/]*version> *//g")

rm -rf build
rm -rf build/lib
mkdir build
mkdir build/lib
cd build/lib || exit
unzip "../../../../jamal-cmd/target/jamal-cmd-${VERSION}-distribution.zip"
cd ..
cd ..
CLASSPATH=$(find build/lib -type f | sed 's#^#/#' | paste -sd ':' -)
cat <<EOL > jamal
#! /usr/bin/env bash
java -cp $CLASSPATH javax0.jamal.cmd.JamalMain \$@
EOL
chmod u+x jamal

CLASSPATH=$(find build/lib -type f | paste -sd ':' -)
cat <<EOL > jamal_local
#! /usr/bin/env bash
java -cp $CLASSPATH javax0.jamal.cmd.JamalMain \$@
EOL
chmod u+x jamal_local
echo "Running Jamal on test file locally"
./jamal_local test.adoc.jam  test1.adoc

if ! cmp -s "test.adoc" "test1.adoc"; then
  echo "Files are different. Exiting."
  exit 1
else
  echo "Files are the same running Jamal local. Continuing."
fi
rm test1.adoc

docker build --build-arg VERSION="$VERSION" -t jamal:"$VERSION" .

echo "Running Jamal on test file from Docker image"
docker run --rm -v .:/workspace -p 5005:5005 jamal:"$VERSION" test.adoc.jam test1.adoc

if ! cmp -s "test.adoc" "test1.adoc"; then
  echo "Files are different. Exiting."
  exit 1
else
  cat <<EOL
Files are the same running Jamal from docker. Done.
You can run the docker image with the command:
    docker run --rm -v \$(pwd):/workspace jamal:$VERSION test.adoc.jam test1.adoc
You can tag this build with:
    docker tag jamal:$VERSION verhas/jamal:$VERSION
  or
    docker tag jamal:$VERSION verhas/jamal:latest

After that you can push it to DockerHub

    docker push verhas/jamal:$VERSION
  or
    docker push verhas/jamal:latest
EOL
fi
rm test1.adoc
