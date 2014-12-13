#!/bin/bash

set -e

mvn -P ossrh-down versions:update-properties -DincludeProperties=jdbc-jboss-modules.version,jxpath-jboss-module.version,dcm4che.version,dcm4chee-storage.version | grep -v 'Downloaded: ' | grep -v 'Downloading: ' | grep -v 'Props: ' | grep -v '[[:digit:]]\+ KB'
mvn -P ossrh-down,db-all install -DskipTests=true | grep -v 'Downloaded: ' | grep -v 'Downloading: ' | grep -v 'Props: ' | grep -v '[[:digit:]]\+ KB'
