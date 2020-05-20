#!/usr/bin/env bash
aws ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 487129032168.dkr.ecr.us-west-2.amazonaws.com
docker build -f Dockerfile-prod -t deciderator-app .
docker tag deciderator-app:latest 487129032168.dkr.ecr.us-west-2.amazonaws.com/deciderator-app:latest
docker push 487129032168.dkr.ecr.us-west-2.amazonaws.com/deciderator-app:latest
