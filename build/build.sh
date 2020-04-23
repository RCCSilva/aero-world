#!/usr/bin/env bash

set -euo pipefail

clojure -A:depstar -m hf.depstar.uberjar target/aero-world-standalone.jar