# does not work...
# . ../../../../../dcm4chee-arc-conf-test/src/test/filters/opendj.properties

LDAP_BASE=/C/Tools/OpenDJ-2.5.0-Xpress1/bin
$LDAP_BASE/ldapsearch -D "cn=Directory Manager" -w1 -b"cn=Devices,cn=DICOM Configuration,dc=example,dc=com"  "!(objectclass=dicomDevicesRoot)" > sample-config.ldif

sed -nf unldif.sed sample-config.ldif > sample-config.ldif.processed
rm sample-config.ldif
mv sample-config.ldif.processed sample-config.ldif