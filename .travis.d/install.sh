#!/bin/bash
set -ev

mvn -P ossrh-down versions:update-properties -DincludeProperties=dcm4che.version,dcm4chee-storage.version

# This has been moved to script.sh until ARCH-152 is fixed.
#mvn -P ossrh-down install -DskipTests=true
