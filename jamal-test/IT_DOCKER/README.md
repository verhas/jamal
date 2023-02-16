
# Integration Tests using Docker

This directory contains the integration tests.
Integration tests are executed using Docker containers.

The shell script `integrationtest` is created from the source `integrationtest.jam`.

Some integration tests are included from other subprojects from `it.sh` files.
Those files are developed testing their functionality locally on the development machine.
When they work they are included into the top level `integrationtest.jam` file.


## Executing the integration tests

The tests can be created and executed using the `test.sh` script.
The script will build the Docker image and run the tests.

Prerequisites:

* Docker must be installed and running.

* The script must be executed from the directory containing the `integrationtest` and the `Dockerfile`.

* The docker instance running should have internet connection

  * to GitHub for cloning Jamal source

  * Maven central for building Jamal

## Integration tests implemented

### Basic Maven Compilation

The first step is to build Maven.
Because the container does not have a local repo it will provide a fresh build downloading all dependencies from central.

### Setting Security

When the macro `maven:load` is used, it requires that the configuration in `~/jamal` is secure.
It must not be read or modified by other users.
The test sets the file permissions to the directory and the configuration file in different ways and runs tests.

### Command Line execution

The test runs the command line tool `jamal` with different parameters.
