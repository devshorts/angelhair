#!/usr/bin/env bash

source ./scripts/env_vars.sh

if [ "$DOCKER_MACHINE_NAME" = "" ]; then
    log-warn "warning your DOCKER_MACHINE_NAME environment variable was not set. have you run: echo \"\$(docker-machine env <MACHINE-NAME)\"?"
    DOCKER_MACHINE_NAME=dev
fi

if [ "$DEBUG_JAVA" = "" ]; then
    DEBUG_JAVA=false
fi

if [ "$1" = "-debug" ]; then
    DEBUG_JAVA=true
fi

CWD=$(dirname $0)
if [ `basename $(pwd)` = 'scripts' ]; then
    cd ../
else
    cd `dirname $CWD`
fi

mkdir -p `pwd`/logs/core

GIT_SHA=`git rev-parse --short HEAD`

image="dom-docker.cloud.dev.phx3.gdg:5000/cassandra-queue:${GIT_SHA}_dev"

echo ${image}

docker run -it \
    -e HOST_IPADDR=`docker-machine ip $DOCKER_MACHINE_NAME` \
    -e CLUSTER_NAME=${CLUSTER_NAME} \
    -e KEYSPACE=${KEYSPACE} \
    -e CONTACT_POINTS=${CONTACT_POINTS} \
    -e USERNAME=${USERNAME} \
    -e PASSWORD=${PASSWORD} \
    -e USE_SSL=${USE_SSL} \
    -e DATA_CENTER=${DATA_CENTER} \
    -e USE_METRICS_GRAPHITE=${USE_METRICS_GRAPHITE} \
    -e GRAPHITE_PREFIX=${GRAPHITE_PREFIX} \
    -e GRAPHITE_URL=${GRAPHITE_URL} \
    -p 8080:8080 \
    -p 8081:8081 \
    -p 1044:1044 \
    -p 1898:1898 \
    -v `pwd`/logs/core:/data/logs \
    -v `pwd`/core/docker/data/conf:/data/conf \
    ${image} "$@"
