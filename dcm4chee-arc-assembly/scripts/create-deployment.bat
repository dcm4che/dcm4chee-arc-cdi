@echo off
set string=mysql:oracle:firebird:h2:db2:sqlserver:psql
cd ..\..
IF EXIST .\dcm4chee-arc-assembly\Assemblies rmdir /s /q .\dcm4chee-arc-assembly\Assemblies
mkdir .\dcm4chee-arc-assembly\Assemblies
for %%x in (%string::= %) do ( 
echo building %%x
mvn clean install -Dldap=apacheds -Ddb=%%x -DskipTests=true
echo assembled %%x
copy .\dcm4chee-arc-assembly\target\*.zip .\dcm4chee-arc-assembly\Assemblies\
)
pause