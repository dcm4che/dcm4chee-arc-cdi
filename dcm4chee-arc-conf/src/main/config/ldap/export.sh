#!/bin/sh

LDAP_SEARCH=${LDAP_SEARCH:-$HOME/opendj/bin/ldapsearch}
SED=${SED:-sed}
HOST=${HOST:-localhost}
PORT=${PORT:-1389}
ROOT_DN=${ROOT_DN:-"dc=example,dc=com"}
BIND_DN=${BIND_DN:-"cn=Directory Manager"}
BIND_PW=${BIND_PW:-secret}

BASE_DN="cn=Devices,cn=DICOM Configuration,$ROOT_DN"
FILTER='(!(objectclass=dicomDevicesRoot))'

DIRNAME="`dirname "$0"`"
UNLDIF_SED=$DIRNAME/unldif.sed
SAMPLE_CONFIG_LDIF=$DIRNAME/sample-config.ldif

$LDAP_SEARCH -h $HOST -p $PORT -D "$BIND_DN" -w $BIND_PW -b "$BASE_DN" "$FILTER" \
| $SED -nf $UNLDIF_SED > $SAMPLE_CONFIG_LDIF
