#!/bin/bash
set -ev

mvn -P ossrh-down versions:update-properties -DincludeProperties=dcm4che.version,dcm4chee-storage.version
mvn -P ossrh-down install -DskipTests=true
