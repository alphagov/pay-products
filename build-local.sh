#!/bin/bash

set -e

cd "$(dirname "$0")"

if [ "$(uname -m)" == "arm64" ]; then
  docker build -t governmentdigitalservice/pay-products:local -f m1/arm64.Dockerfile .
else
  docker build -t governmentdigitalservice/pay-products:local .
fi
