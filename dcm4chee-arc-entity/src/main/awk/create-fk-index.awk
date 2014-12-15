#!/usr/bin/awk -f
$2 == "table" { table = $3 }
$2 == "constraint" {constraint = $3 }
$1 == "foreign" && $3 != "(dicomattrs_fk)" {print "create index", constraint, "on", table, $3 ";"}
