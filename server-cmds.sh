#!/bin/bash

expose IMAGE=$1
docker-compose -f docker-compose.yaml up -d
echo "success"