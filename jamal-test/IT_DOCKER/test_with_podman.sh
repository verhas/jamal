#!/bin/sh
podman build -t jamal-test .
podman run -it jamal-test
