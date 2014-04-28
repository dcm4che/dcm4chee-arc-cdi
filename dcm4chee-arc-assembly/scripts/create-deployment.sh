declare -a arr=( "mysql" "oracle" "firebird" "h2" "db2" "sqlserver" "psql")
cd ../..
rm -rf ./dcm4chee-arc-assembly/Assemblies
mkdir ./dcm4chee-arc-assembly/Assemblies

for i in "${arr[@]}"
do
  echo "building $i"
mvn clean install -Dldap=apacheds -Ddb=$i -DskipTests=true
  echo "assembled $i"
  cp ./dcm4chee-arc-assembly/target/*.zip ./dcm4chee-arc-assembly/Assemblies/
done
