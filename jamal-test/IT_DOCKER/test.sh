#!/bin/bash
#
# This shell script prepares the docker container and other scrips needed to execute the 'intergration' test script.
#
set -e

#
# The code in Github runs from the root directory
#
if [[ -e ".mvn" ]]; then
  echo
  cd jamal-test/IT_DOCKER
  GITHUB=1
else
  GITHUB=0
fi

#
# Create the dockerfile
#
cat > Dockerfile <<END
FROM alpine
RUN apk update
RUN apk add --no-cache openjdk17 maven git python3 py3-pip
RUN addgroup TESTGROUP
RUN adduser -G TESTGROUP -D -s /bin/bash jamal

WORKDIR /home/jamal
COPY integrationtest .
RUN chown jamal:TESTGROUP integrationtest
RUN chmod u+xr integrationtest
USER jamal
CMD ./integrationtest
END

#
# Build the docker image
#
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
echo "building docker image"
docker run -it jamal-test | sed -r "s/\x1B\[[0-9;]*[mK]//g" | tee -a integration_test.log

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
echo "sleep a second to ensure log files are closed and flushed"
sleep 1

#
# Delete the progress lines from the log (approx 27k lines to 4k)
#
sed -i -E '/Progress/d; /Receiving objects:/d; /remote: Counting objects:/d; /remote: Compressing objects:/d; /Resolving deltas:/d' integration_test.log || echo "filtering failed"

if [[ $GITHUB -eq 1 ]]; then
  cat integration_test.log
fi

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