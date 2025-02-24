#!/bin/bash
#
# This shell script prepares the docker container and other scrips needed to execute the 'integration' test script.
#
set -e

#
# The code in Github runs from the root directory
#
if [[ -e ".mvn" ]]; then
  echo "We are running in GitHub action"
  cd jamal-test/IT_DOCKER
  GITHUB=1
else
  GITHUB=0
fi

#
# Create a temporary directory to store the Git-tracked files
#
mkdir -p ./build_temp
cd build_temp || exit 1
TEMP_DIR=$(pwd)
# shellcheck disable=SC2064
trap "rm -rf $TEMP_DIR" EXIT

# Copy only the files tracked by Git
echo "Copying Git-tracked files to temporary directory"
cd ../../..
git ls-files | tar -cf - -T - | tar -xf - -C "$TEMP_DIR"
cd $TEMP_DIR/..

#
# Create the dockerfile
#
cat > Dockerfile <<END
FROM alpine
RUN apk update
RUN apk add --no-cache bash openjdk17 maven git python3 py3-pip
RUN addgroup TESTGROUP
RUN adduser -G TESTGROUP -D -s /bin/bash jamal

WORKDIR /home/jamal
COPY --chown=jamal:TESTGROUP integrationtest .
COPY --chown=jamal:TESTGROUP ./build_temp /home/jamal/jamal
RUN chmod u+xr integrationtest
USER jamal
CMD ./integrationtest
END

#
# Build the docker image
#
echo "building docker image"
if ! docker build -t jamal-test .
then
  echo "Docker build failed. Exiting."
  exit 1
fi
# clean up
rm Dockerfile || exit 1

#
# Run the integration test
#
START_TIME=$(date +%s)

if [[ -f integration_test.log ]]; then
  mv integration_test.log integration_test.log.BAK
fi
echo "running docker image"

docker run jamal-test | sed -E "s/\x1B\[[0-9;]*[mK]//g; /Progress/d; /Receiving objects:/d; /remote: Counting objects:/d; /remote: Compressing objects:/d; /Resolving deltas:/d" | tee -a integration_test.log

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
echo "sleep a second to ensure log files are closed and flushed"
sleep 1

if tail -n1 integration_test.log | grep -q "INTEGRATION TEST SUCCESSFUL"; then
    RESULT=0
else
    RESULT=1
fi

if [[ $RESULT -eq 0 ]]; then
    echo "OK $(date '+%Y-%m-%d %H:%M:%S')" > integration_test.run
    echo "INTEGRATION TEST OK $DURATION sec"
    exit 0
else
    echo "FAIL $(date '+%Y-%m-%d %H:%M:%S')" > integration_test.run
    echo "FAIL $DURATION sec"
    exit 1
fi