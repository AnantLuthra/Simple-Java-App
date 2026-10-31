#!/bin/bash

export IMAGE=$1
export PASSWORD=$2
export USERNAME=$3

echo $PASSWORD | docker login -u $USERNAME --password-stdin
docker-compose -f docker-compose.yaml up -d
echo "success"