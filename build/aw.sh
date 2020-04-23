#!/usr/bin/env bash

set -euo pipefail

java -cp target/aero-world-standalone.jar clojure.main -m aero-world.core