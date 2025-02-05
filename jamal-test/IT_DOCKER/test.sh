#!/bin/sh
# DO NOT EDIT 
set -e

cat > ../../jamal.local.dev.sh <<"END"
#!/bin/sh

set -e  # Exit immediately if a command exits with a non-zero status
LIB_DIR="/Users/verhasp/github/jamal/.lib"

rm -rf "$LIB_DIR"
mkdir "$LIB_DIR"
unzip -q "/Users/verhasp/github/jamal/jamal-cmd/target/jamal-cmd-2.8.3-SNAPSHOT-distribution.zip" -d "$LIB_DIR"
CLASSPATH=$(find "$LIB_DIR" -name "*.jar" | tr '\n' ':')
java -cp "$CLASSPATH" javax0.jamal.cmd.JamalMain "$@"
rm -rf "$LIB_DIR"
END

if [ ! -f "../../jamal.local.dev.sh" ]; then
echo "Error: ../../jamal.local.dev.sh not found!"
exit 1
fi

chmod 0700 ../../jamal.local.dev.sh
../../jamal.local.dev.sh ../../jamal-maven-load/it.sh.jam ../../jamal-maven-load/it.sh
../../jamal.local.dev.sh integrationtest.jam integrationtest
../../jamal.local.dev.sh README.adoc.jam README.adoc


if [ ! -f "test_groovy.sh.jam.jam" ]; then
    echo "Error: test_groovy.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_groovy.sh.jam.jam test_groovy.sh.jam 
if [ ! -f "test_ruby.sh.jam.jam" ]; then
    echo "Error: test_ruby.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_ruby.sh.jam.jam test_ruby.sh.jam 
if [ ! -f "test_py.sh.jam.jam" ]; then
    echo "Error: test_py.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_py.sh.jam.jam test_py.sh.jam 
if [ ! -f "test_scriptbasic.sh.jam.jam" ]; then
    echo "Error: test_scriptbasic.sh.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_scriptbasic.sh.jam.jam test_scriptbasic.sh.jam 

if [ ! -f "test_groovy.txt.jam.jam" ]; then
    echo "Error: test_groovy.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_groovy.txt.jam.jam test_groovy.txt.jam 
if [ ! -f "test_ruby.txt.jam.jam" ]; then
    echo "Error: test_ruby.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_ruby.txt.jam.jam test_ruby.txt.jam 
if [ ! -f "test_py.txt.jam.jam" ]; then
    echo "Error: test_py.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_py.txt.jam.jam test_py.txt.jam 
if [ ! -f "test_scriptbasic.txt.jam.jam" ]; then
    echo "Error: test_scriptbasic.txt.jam.jam does not exist."
    exit 1
fi
../../jamal.local.dev.sh -T7 test_scriptbasic.txt.jam.jam test_scriptbasic.txt.jam 


docker build -t jamal-test .
if [ $? -ne 0 ]; then
  echo "Docker build failed. Exiting."
  exit 1
fi
docker run -it jamal-test