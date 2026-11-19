#!/bin/sh
set -eu
set -a
. ./.env
set +a
mvn spring-boot:run
