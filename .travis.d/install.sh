#!/bin/bash
set -ev

mvn -P ossrh-down versions:update-properties -DincludeProperties=jdbc-jboss-modules.version,jxpath-jboss-module.version,dcm4che.version,dcm4chee-storage.version
mvn -P ossrh-down,db-all install -DskipTests=true
