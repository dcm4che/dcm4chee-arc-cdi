Getting Started with DCM4CHEE Archive 4.3.0.Alpha4
==================================================

Requirements
------------
-   Java SE 6 or later - tested with [OpenJDK](http://openjdk.java.net/)
    and [Oracle JDK](http://java.com/en/download)

-   [JBoss Application Server WildFly 8.0.0.Final](http://wildfly.org/downloads/)

-   Supported SQL Database:
    - [MySQL 5.5](http://dev.mysql.com/downloads/mysql)
    - [PostgreSQL 9.2.4](http://www.postgresql.org/download/)
    - [Firebird 2.5.1](http://www.firebirdsql.org/en/firebird-2-5-1/)
    - [DB2 10.1](http://www-01.ibm.com/software/data/db2/express/download.html)
    - [Oracle 11g](http://www.oracle.com/technetwork/products/express-edition/downloads/)
    - [Microsoft SQL Server](http://www.microsoft.com/en-us/download/details.aspx?id=29062)
      (not yet tested!)

-   LDAP Server - tested with
    - [OpenDJ 2.5.0-Xpress1](http://www.forgerock.org/opendj.html),
    - [OpenLDAP 2.4.35](http://www.openldap.org/software/download/) and
    - [Apache DS 2.0.0-M16](http://directory.apache.org/apacheds/downloads.html).

    *Note*: DCM4CHEE Archive 4.3.0.Alpha4 also supports using Java Preferences 
    as configuration backend. But because DCM4CHEE Archive 4.3. does not yet
    contain a configuration front-end, you would have to edit configuration 
    entries in the Java Preferences back-end manually, which is quit cumbersome 
    and therefore not further described here.

-   LDAP Browser - [Apache Directory Studio 2.0.0-M8](http://directory.apache.org/studio/)

    *Note*: Because DCM4CHEE Archive 4.3.0.Alpha4 does not yet contain a specific
    configuration front-end, the LDAP Browser is needed to modify the archive
    configuration.


Download and extract binary distribution package
------------------------------------------------
DCM4CHEE Archive 4.x binary distributions for different databases can be obtained
from [Sourceforge](https://sourceforge.net/projects/dcm4che/files/dcm4chee-arc4/).
Extract (unzip) your chosen download to the directory of your choice.


Initialize Database
-------------------
*Note*: DCM4CHEE Archive 4.3.0.Alpha4 does not provide SQL scripts and utilities to
migrate DCM4CHEE Archive 2.x data base schema to DCM4CHEE Archive 4.x. There will be
provided by DCM4CHEE Archive 4.x final releases.

### MySQL

1. Enable remote access by commenting out `skip-networking` in configuration file `my.conf`.

2. Create database and grant access to user

        > mysql -u root -p<root-password>
        mysql> CREATE DATABASE <database-name>;
        mysql> GRANT ALL ON <database-name>.* TO '<user-name>' IDENTIFIED BY '<user-password>';
        mysql> quit

3. Create tables and indexes
       
        > mysql -u <user-name> -p<user-password> <database-name> < $DCM4CHEE_ARC/sql/create-table-mysql.ddl
        > mysql -u <user-name> -p<user-password> <database-name> < $DCM4CHEE_ARC/sql/create-index.ddl


### PostgreSQL

1. Create user with permission to create databases 

        > createuser -U postgres -P -d <user-name>
        Enter password for new role: <user-password> 
        Enter it again: <user-password> 

2. Create database

        > createdb -U <user-name> <database-name>

3. Create tables and indexes
       
        > psql -U <user-name> < $DCM4CHEE_ARC/sql/create-table-psql.ddl
        > psql -U <user-name> < $DCM4CHEE_ARC/sql/create-index.ddl


### Firebird

1. Define database name in configuration file `aliases.conf`:

        <database-name> = <database-file-path>

2. Create user

        > gsec -user sysdba -password masterkey \
          -add <user-name> -pw <user-password>

3. Create database, tables and indexes

        > isql 
        Use CONNECT or CREATE DATABASE to specify a database
        SQL> CREATE DATABASE 'localhost:<database-name>'
        CON> user '<user-name>' password '<user-password>';
        SQL> IN $DCM4CHEE_ARC/sql/create-table-firebird.ddl;
        SQL> IN $DCM4CHEE_ARC/sql/create-index.ddl;
        SQL> EXIT;

        
### DB2

1. Create database and grant authority to create tables to user
   (must match existing OS user)

        > sudo su db2inst1
        > db2
        db2 => CREATE DATABASE <database-name> PAGESIZE 16 K
        db2 => connect to <database-name>
        db2 => GRANT CREATETAB ON DATABASE TO USER <user-name>
        db2 => terminate
 
2. Create tables and indexes

        > su <user-name>
        Password: <user-password>
        > db2 connect to <database-name>
        > db2 -t < $DCM4CHEE_ARC/sql/create-table-db2.ddl
        > db2 -t < $DCM4CHEE_ARC/sql/create-index.ddl
        > db2 terminate
        

### Oracle 11g 

1. Connect to Oracle and create a new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE BIGFILE TABLESPACE <tablespace-name> DATAFILE '<data-file-location>' SIZE <size>;

        Tablespace created.

2. Create a new user with privileges for the new tablespace

        $ sqlplus / as sysdba
        SQL> CREATE USER <user-name> 
        2  IDENTIFIED BY <user-password>
        3  DEFAULT TABLESPACE <tablespace-name>
        4  QUOTA UNLIMITED ON <tablespace-name>
        5  QUOTA 50M ON SYSTEM;

        User created.

        SQL> GRANT CREATE SESSION TO <user-name>;
        SQL> GRANT CREATE TABLE TO <user-name>;
        SQL> GRANT CREATE ANY INDEX TO <user-name>;
        SQL> GRANT CREATE SEQUENCE TO <user-name>;
        SQL> exit

3. Create tables and indexes

        $ sqlplus <user-name>/<user-password>
        SQL> @$DCM4CHEE_ARC/sql/create-table-oracle.ddl
        SQL> @$DCM4CHEE_ARC/sql/create-index.ddl

Setup LDAP Server
-----------------

### OpenDJ

1.  Copy LDAP schema files for OpenDJ from DCM4CHEE Archive distribution to
    OpenDJ schema configuration directory:

        > cp $DCM4CHEE_ARC/ldap/opendj/* $OPENDJ_HOME/config/schema/ [UNIX]
        > copy %DCM4CHEE_ARC%\ldap\opendj\* %OPENDJ_HOME%\config\schema\ [Windows]

2.  Run OpenDJ GUI based setup utility

        > $OPENDJ_HOME/setup
    
    Log the values choosen for
    -  LDAP Listener port (1389)
    -  Root User DN (cn=Directory Manager)
    -  Root User Password (secret)
    -  Directory Base DN (dc=example,dc=com)

    needed for the LDAP connection configuration of DCM4CHEE Archive.

4. After initial setup, you may start and stop OpenDJ by

        > $OPENDJ_HOME/bin/start-ds
        > $OPENDJ_HOME/bin/stopt-ds


### OpenLDAP

OpenLDAP binary distributions are available for most Linux distributions and
for [Windows](http://www.userbooster.de/en/download/openldap-for-windows.aspx).

OpenLDAP can be alternatively configured by

- [slapd.conf configuration file](http://www.openldap.org/doc/admin24/slapdconfig.html)
- [dynamic runtime configuration](http://www.openldap.org/doc/admin24/slapdconf2.html)

See also [Converting old style slapd.conf file to cn=config format][1]

[1]: http://www.openldap.org/doc/admin24/slapdconf2.html#Converting%20old%20style%20{{slapd.conf}}%285%29%20file%20to%20{{cn=config}}%20format

#### OpenLDAP with slapd.conf configuration file

1.  Copy LDAP schema files for OpenLDAP from DCM4CHEE Archive distribution to
    OpenLDAP schema configuration directory:

        > cp $DCM4CHEE_ARC/ldap/schema/* /etc/openldap/schema/ [UNIX]
        > copy %DCM4CHEE_ARC%\ldap\schema\* \Program Files\OpenLDAP\schema\ [Windows]

2.  Add references to schema files in `slapd.conf`, e.g.:

        include         /etc/openldap/schema/core.schema
        include         /etc/openldap/schema/dicom.schema
        include         /etc/openldap/schema/dcm4che.schema
        include         /etc/openldap/schema/dcm4chee-archive.schema

3.  You may also change the default values for 

        suffix          "dc=my-domain,dc=com"
        rootdn          "cn=Manager,dc=my-domain,dc=com"
        rootpw          secret
   
    in `slapd.conf`.


#### OpenLDAP with dynamic runtime configuration

1.  Import LDAP schema files for OpenLDAP runtime configuration, binding as
    root user of the config backend, using OpenLDAP CL utility ldapadd, e.g.:

        > ldapadd -xW -Dcn=config -f $DCM4CHEE_ARC/ldap/slapd/dicom.ldif
        > ldapadd -xW -Dcn=config -f $DCM4CHEE_ARC/ldap/slapd/dcm4che.ldif
        > ldapadd -xW -Dcn=config -f $DCM4CHEE_ARC/ldap/slapd/dcm4chee-archive.ldif

    If you don't know the root user and its password of the config backend, you may
    look into `/etc/openldap/slap.d/cn=config/olcDatabase={0}config.ldif`:

        olcRootDN: cn=config
        olcRootPW:: VmVyeVNlY3JldA==

    and decode the base64 decoded password, e.g:

        > echo -n VmVyeVNlY3JldA== | base64 -d
        VerySecret

    If there is no `olcRootPW` entry, you may just add one.

    You may also specify the password in plan text, e.g:

        olcRootPW: VerySecret

2.  Directory Base DN and Root User DN can be modified by changing the values of
    attributes

        olcSuffix: dc=my-domain,dc=com
        olcRootDN: cn=Manager,dc=my-domain,dc=com

    of object `olcDatabase={1}hdb,cn=config` by specifing the new values in a 
    LDIF file (e.g. `modify-baseDN.ldif`)

        dn: olcDatabase={1}hdb,cn=config
        changetype: modify
        replace: olcSuffix
        olcSuffix: dc=example,dc=com
        -
        replace: olcRootDN
        olcRootDN: cn=Manager,dc=example,dc=com
        -

    and applying it using OpenLDAP CL utility ldapmodify, e.g.:

        > ldapmodify -xW -Dcn=config -f modify-baseDN.ldif

### Apache DS 2.0.0

1.  Install [Apache DS 2.0.0-M16](http://directory.apache.org/apacheds/downloads.html)
    on your system and start Apache DS.

2.  Install [Apache Directory Studio 2.0.0-M8](http://directory.apache.org/studio/) and
    create a new LDAP Connection with:

        Network Parameter:
            Hostname: localhost
            Port:     10398
        Authentication Parameter:
            Bind DN or user: uid=admin,ou=system
            Bind password:   secret

3.  Import LDAP schema files for Apache DS:

        $DCM4CHEE_ARC/ldap/apacheds/dicom.ldif
        $DCM4CHEE_ARC/ldap/apacheds/dcm4che.ldif
        $DCM4CHEE_ARC/ldap/apacheds/dcm4chee-archive.ldif

    using the LDIF import function of Apache Directory Studio LDAP Browser.

4.  You may modify the default Directory Base DN `dc=example,dc=com` by changing
    the value of attribute 

        ads-partitionsuffix: dc=example,dc=com`

    of object

        ou=config
        + ads-directoryServiceId=default
          + ou=partitions
              ads-partitionId=example
    
    using Apache Directory Studio LDAP Browser.


Import sample configuration into LDAP Server
--------------------------------------------  

1.  If not alread done, install
    [Apache Directory Studio 2.0.0-M8](http://directory.apache.org/studio/) and create
    a new LDAP Connection corresponding to your LDAP Server configuration, e.g:

        Network Parameter:
            Hostname: localhost
            Port:     1398
        Authentication Parameter:
            Bind DN or user: cn=Directory Manager
            Bind password:   secret
        Browser Options:
            Base DN: dc=example,dc=com

2.  If you configured a different Directory Base DN than`dc=example,dc=com`,
    you have to replace all occurrences of `dc=example,dc=com` in LDIF files
 
        $DCM4CHEE_ARC/ldap/init-baseDN.ldif
        $DCM4CHEE_ARC/ldap/init-config.ldif
        $DCM4CHEE_ARC/ldap/sample-config.ldif

    by your Directory Base DN, e.g.:

        > cd $DCM4CHEE_ARC/ldap
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ init-baseDN.ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ init-config.ldif
        > sed -i s/dc=example,dc=com/dc=my-domain,dc=com/ sample-config.ldif

3.  If there is not already a base entry in the directory data base, import
    `$DCM4CHEE_ARC/ldap/init-baseDN.ldif` using the LDIF import function of
    Apache Directory Studio LDAP Browser.

4.  If there are not already DICOM configuration root entries in the directory
    data base, import `$DCM4CHEE_ARC/ldap/init-config.ldif` using the LDIF import
    function of Apache Directory Studio LDAP Browser.  

5.  Import `$DCM4CHEE_ARC/ldap/sample-config.ldif` using the LDIF import function
    of Apache Directory Studio LDAP Browser.  

6.  By default configuration, DCM4CHEE Archive does not accept remote connections.
    To enable remote connections, replace the value of attribute

        dicomHostname=localhost
    
    of the 4 `dicomNetworkConnection` objects

        dc=example,dc=com
        + cn=DICOM Configuration
          + cn=Devices
            + dicomDeviceName=dcm4chee-arc
                cn=dicom
                cn=dicom-tls
                cn=hl7,dicomDeviceName
                cn=hl7-tls,dicomDeviceName

    by the actual hostname of your system, using Apache Directory Studio LDAP Browser. 

7.  To change the default AE Title: `DCM4CHEE`, modify the _Relative Distinguished Name_
    (RDN) of the `dicomNetworkAE` object
        
        dc=example,dc=com
        + cn=DICOM Configuration
          + cn=Devices
            + dicomDeviceName=dcm4chee-arc
                dicomAETitle=DCM4CHEE

     to

        dicomAETitle=<aet>

     You may also modify the AE Title: `DCM4CHEE_ADMIN` of the additional AE:

        dc=example,dc=com
        + cn=DICOM Configuration
          + cn=Devices
            + dicomDeviceName=dcm4chee-arc
                dicomAETitle=DCM4CHEE_ADMIN

    which is configured to provide also Access to Rejected Instances for
    Quality Reasons, as specified by IHE Radiology Technical Framework 
    Supplement [Imaging Object Change Management (IOCM)][2].

[2]: http://www.ihe.net/Technical_Framework/upload/IHE_RAD_Suppl_IOCM_Rev1-1_TI_2011-05-17.pdf

8.  By default configuration, DCM4CHEE Archive stores received objects below
    `$JBOSS_HOME/standalone/data`. To specify a different storage location,
    replace the value of attribute

        dcmInitFileSystemURI=${jboss.server.data.url}

    of the `dicomNetworkAE` object

        dc=example,dc=com
        + cn=DICOM Configuration
          + cn=Devices
            + dicomDeviceName=dcm4chee-arc
                dicomAETitle=DCM4CHEE

    by `file:<absolute-directory-path-with-slashes>`, using Apache Directory Studio LDAP Browser.

    **Attention**: The value of attribute `dcmInitFileSystemURI` is used to initialize
    the (first) record in the `filesystem` table of the archive data base on receive of
    the first object. It is not effective, if the `filesystem` table already contains
    such record.


Setup WildFly
--------------

1.  Adjust DCM4CHEE Archive LDAP Connection configuration file
    `$DCM4CHEE_ARC/configuration/dcm4chee-arc/ldap.properties`:

        java.naming.factory.initial=com.sun.jndi.ldap.LdapCtxFactory
        java.naming.ldap.attributes.binary=dicomVendorData
        java.naming.provider.url=ldap://localhost:1389/dc=example,dc=com
        java.naming.security.principal=cn=Directory Manager
        java.naming.security.credentials=secret

    to your LDAP Server configuration.

2.  Copy configuration files into the WildFly installation:

        > cp -r $DCM4CHEE_ARC/configuration/dcm4chee-arc $JBOSS_HOME/standalone/configuration [UNIX]
        > xcopy %DCM4CHEE_ARC%\configuration\dcm4chee-arc %JBOSS_HOME%\standalone\configuration [Windows]

    *Note*: Beside LDAP Connection configuration, the private key used in TLS connections
    and XSLT stylesheets specifing attribute coercion in incoming or outgoing DICOM messages
    and mapping of HL7 fields in incoming HL7 messages are not stored in LDAP
	A keystore file is provided with the sample config under $DCM4CHEE_ARC/configuration/dcm4chee-arc
	the password for the given keystore is "changeit" and can be set in the LDAP or java preferences configuration
	by setting the attribute dcmKeyStorePin to change it instead of secret as provided in the sample configuration.

3.  Install DCM4CHE 3.3.3 libraries as WildFly module:

        > cd  $JBOSS_HOME
        > unzip $DCM4CHEE_ARC/jboss-module/dcm4che-jboss-modules-3.3.3.zip

4.  Install JAI Image IO 1.2 libraries as WildFly module
    (needed for compression/decompression, does not work on Windows 64 bit
    and Mac OS X caused by missing native components for these platforms):

        > cd  $JBOSS_HOME
        > unzip $DCM4CHEE_ARC/jboss-module/jai_imageio-jboss-modules-1.2-pre-dr-b04.zip

		Latest version of the native libraries can be downloaded from

		[jai-download](http://download.java.net/media/jai/builds/release/1_1_3/)
		
5.  Install QueryDSL 3.2.3 libraries as WildFly module:

        > cd  $JBOSS_HOME
        > unzip $DCM4CHEE_ARC/jboss-module/querydsl-jboss-modules-3.2.3.zip

6.  Install JDBC Driver. DCM4CHEE Archive 4.x binary distributions do not include
    a JDBC driver for the database for license issues. You may download it from:
    -   [MySQL](http://www.mysql.com/products/connector/)
    -   [PostgreSQL]( http://jdbc.postgresql.org/)
    -   [Firebird](http://www.firebirdsql.org/en/jdbc-driver/)
    -   [DB2](http://www-306.ibm.com/software/data/db2/java/), also included in DB2 Express-C
    -   [Oracle](http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/index.htm),
        also included in Oracle 11g XE)
    -   [Microsoft SQL Server](http://msdn.microsoft.com/data/jdbc/)

    The JDBC driver can be installed either as a deployment or as a core module.
    [See](https://docs.jboss.org/author/display/WFLY8/Developer+Guide#DeveloperGuide-InstalltheJDBCdriver)
    
    Installation as deployment is limited to JDBC 4-compliant driver consisting of **one** JAR.

    For installation as a core module, `$DCM4CHEE_ARC/jboss-module/jdbc-jboss-modules-1.0.0-<database>.zip`
    already provides a module definition file `module.xml`. You just need to extract the ZIP file into
    $JBOSS_HOME and copy the JDBC Driver file(s) into the sub-directory, e.g.:

        > cd $JBOSS_HOME
        > unzip $DCM4CHEE_ARC/jboss-module/jdbc-jboss-modules-1.0.0-db2.zip
        > cd $DB2_HOME/java
        > cp db2jcc4.jar db2jcc_license_cu.jar $JBOSS_HOME/modules/com/ibm/db2/main/

    Verify, that the actual JDBC Driver file(s) name matches the path(s) in the provided
    `module.xml`, e.g.:

         <?xml version="1.0" encoding="UTF-8"?>
         <module xmlns="urn:jboss:module:1.1" name="com.ibm.db2">
             <resources>
                 <resource-root path="db2jcc4.jar"/>
                 <resource-root path="db2jcc_license_cu.jar"/>
             </resources>
         
             <dependencies>
                 <module name="javax.api"/>
                 <module name="javax.transaction.api"/>
             </dependencies>
         </module>


7.  Start WildFly in standalone mode with the Java EE 6 Full Profile configuration.
    To preserve the original WildFly configuration you may copy the original
    configuration file for JavaEE 6 Full Profile:

        > cd $JBOSS_HOME/standalone/configuration/
        > cp standalone-full.xml dcm4chee-arc.xml

    and start WildFly specifying the new configuration file:
        
        > $JBOSS_HOME/bin/standalone.sh -c dcm4chee-arc.xml [UNIX]
        > %JBOSS_HOME%\bin\standalone.bat -c dcm4chee-arc.xml [Windows]
   
    Verify, that WildFly started successfully, e.g.:

		===============================================================================

		JBoss Bootstrap Environment

		JBOSS_HOME: "D:\Users\aprta\Documents\Servers\wildfly-8.0.0.Final"

		JAVA: "C:\Program Files\Java\jdk1.7.0_51\bin\java"

		JAVA_OPTS: "-XX:+UseCompressedOops -Dprogram.name=standalone.bat -Xms64M -Xmx512M -XX:MaxPermSize=256M 
		-Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman"

		===============================================================================
        
		13:04:01,146 INFO  [org.jboss.modules] (main) JBoss Modules version 1.3.0.Final
		13:04:01,348 INFO  [org.jboss.msc] (main) JBoss MSC version 1.2.0.Final
		13:04:01,412 INFO  [org.jboss.as] (MSC service thread 1-6) JBAS015899: WildFly 8.0.0.Final "WildFly" starting
        :
        13:04:02,744 INFO  [org.jboss.as] (Controller Boot Thread) JBAS015874: WildFly 8.0.0.Final "WildFly" started in 1826ms - Started 183 of 232 services (80 services are lazy, passive or on-demand)
                
    Running WildFly in domain mode should work, but was not yet tested.

8.  Add JDBC Driver into the server configuration using WildFly CLI in a new console window:

        > $JBOSS_HOME/bin/jboss-cli.sh -c [UNIX]
        > %JBOSS_HOME%\bin\jboss-cli.bat -c [Windows]
        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=<driver-name>:add(driver-name=<driver-name>,driver-module-name=<module-name>)

    You may choose any `<driver-name>` for the JDBC Driver, `<module-name>` must match the name
    defined in the module definition file `module.xml` of the JDBC driver, e.g.:

        [standalone@localhost:9999 /] /subsystem=datasources/jdbc-driver=db2:add(driver-module-name=com.ibm.db2)

9.  Create and enable a new Data Source bound to JNDI name `java:/PacsDS` using WildFly CLI:

        [standalone@localhost:9999 /] data-source add --name=PacsDS \
        >     --driver-name=<driver-name> \
        >     --connection-url=<jdbc-url> \
        >     --jndi-name=java:/PacsDS \
        >     --user-name=<user-name> \
        >     --password=<user-password>
        [standalone@localhost:9999 /] data-source enable --name=PacsDS

    The format of `<jdbc-url>` is JDBC Driver specific, e.g.:
    -  MySQL: `jdbc:mysql://localhost:3306/<database-name>`
    -  PostgreSQL: `jdbc:postgresql://localhost:5432/<database-name>`
    -  Firebird: `jdbc:firebirdsql:localhost/3050:<database-name>`
    -  DB2: `jdbc:db2://localhost:50000/<database-name>`
    -  Oracle: `jdbc:oracle:thin:@localhost:1521:<database-name>`
    -  Microsoft SQL Server: `jdbc:sqlserver://localhost:1433;databaseName=<database-name>`

10. Create JMS Queues using WildFly CLI:

        [standalone@localhost:9999 /] jms-queue add --queue-address=ianscu --entries=queue/ianscu
        [standalone@localhost:9999 /] jms-queue add --queue-address=mppsscu --entries=queue/mppsscu
        [standalone@localhost:9999 /] jms-queue add --queue-address=stgcmtscp --entries=queue/stgcmtscp

11. At default, DCM4CHEE Archive 4.x will look for the LDAP connection configuration file at

        $JBOSS_HOME/standalone/configuration/dcm4chee-arc/ldap.properties

    You may specify a different location by system property `org.dcm4chee.archive.ldapPropertiesURL`
    using WildFly CLI:

        [standalone@localhost:9999 /] /system-property=org.dcm4chee.archive.ldapPropertiesURL:add(value=<url>)

    If DCM4CHEE Archive 4.x cannot find the LDAP connection configuration on the specified location, it
    will try to fetch the Archive configuration from Java Preferences.

12. At default, DCM4CHEE Archive 4.x will assume `dcm4chee-arc` as its Device Name, used to find its
    configuration in the configuration backend (LDAP Server or Java Preferences). You may specify a different
    Device Name by system property `org.dcm4chee.archive.deviceName` using WildFly CLI:

        [standalone@localhost:9999 /] /system-property=org.dcm4chee.archive.deviceName:add(value=<device-name>)
		
*Note*:If you do that you can not directly use the sample ldap configuration provided under $DCM4CHEE_ARC/ldap/sample-config.ldif
You will then have to change in each dn the dicomDeviceName=XXX from dcm4chee-arc to the name you specify.

13. JMX MBeans are no longer supported by DCM4CHEE Archive 4.x, however appropriate restful interfaces are available for starting, stopping 
	the archive service as well as reloading the archive configuration.
   
14. Deploy DCM4CHEE Archive 4.x using WildFly CLI, e.g.:

        [standalone@localhost:9999 /] deploy $DCM4CHEE_ARC/deploy/dcm4chee-arc-war-4.3.0.Alpha4-mysql.war

    Verify that DCM4CHEE Archive was deployed and started successfully, e.g.:

        13:52:29,110 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-4) JBAS015876: Starting deployment of "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war" (runtime-name: "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war")
        13:52:30,000 INFO  [org.jboss.as.jpa] (MSC service thread 1-2) JBAS011401: Read persistence.xml for dcm4chee-arc
        :
        13:52:34,284 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-1) Start TCP Listener on localhost/127.0.0.1:11112
		13:52:34,284 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-2) Start TCP Listener on localhost/127.0.0.1:2575
		13:52:34,612 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-3) Start TCP Listener on localhost/127.0.0.1:2762
		13:52:34,613 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-4) Start TCP Listener on localhost/127.0.0.1:12575
        13:52:34,775 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-11) JBAS017534: Registered web context: /dcm4chee-arc
        13:52:34,814 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 32) JBAS018559: Deployed "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war" (runtime-name : "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war")

15. You may undeploy DCM4CHEE Archive at any time using WildFly CLI, e.g.:

        [standalone@localhost:9999 /] undeploy dcm4chee-arc-war-4.3.0.Alpha4-mysql.war

        13:59:14,861 INFO  [org.jboss.weld.deployer] (MSC service thread 1-2) JBAS016009: Stopping weld service for deployment dcm4chee-arc-war-4.3.0.Alpha4-mysql.war
		13:59:14,863 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-1) Stop TCP Listener on localhost/127.0.0.1:11112
		13:59:14,864 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-4) Stop TCP Listener on localhost/127.0.0.1:12575
		13:59:14,864 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-3) Stop TCP Listener on localhost/127.0.0.1:2762
		13:59:14,864 INFO  [org.dcm4che3.net.Connection] (EE-ManagedExecutorService-default-Thread-2) Stop TCP Listener on localhost/127.0.0.1:2575
		..
		13:59:14,895 INFO  [org.jboss.as.jpa] (ServerService Thread Pool -- 74) JBAS011410: Stopping Persistence Unit (phase 1 of 2) Service 'dcm4chee-arc-war-4.3.0.Alpha4-mysql.war#dcm4chee-arc'
		13:59:14,927 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-14) JBAS015877: Stopped deployment dcm4chee-arc-war-4.3.0.Alpha4-mysql.war (runtime-name: dcm4chee-arc-war-4.3.0.Alpha4-mysql.war) in 110ms
		13:59:14,983 INFO  [org.jboss.as.server] (management-handler-thread - 2) JBAS018558: Undeployed "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war" (runtime-name: "dcm4chee-arc-war-4.3.0.Alpha4-mysql.war")

16. You may deploy the web interface using the provided dcm4chee-arc-web-4.3.0.Alpha4-mysql.war using wildfly CLI, e.g.:

		[standalone@localhost:9999 /] deploy $DCM4CHEE_ARC/deploy/dcm4chee-arc-web-4.3.0.Alpha4-mysql.war
		
		14:04:46,081 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-16) JBAS015876: Starting deployment of "dcm4chee-arc-web-4.3.0.alpha4-mysql.war" (runtime-name: "dcm4chee-arc-web-4.3.0.alpha4-mysql.war")
		14:04:46,185 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-15) JBAS017534: Registered web context: /dcm4chee-web
		14:04:46,210 INFO  [org.jboss.as.server] (management-handler-thread - 12) JBAS018559: Deployed "dcm4chee-arc-web-4.3.0.alpha4-mysql.war" (runtime-name: "dcm4chee-arc-web-4.3.0.alpha4-mysql.war")
		
17. You may undeply the web interface at any time using the WildFly CLI, e.g.:

		[standalone@localhost:9999 /] undeploy dcm4chee-arc-web-4.3.0.Alpha4-mysql.war

		14:08:19,399 INFO  [org.wildfly.extension.undertow] (MSC service thread 1-15) JBAS017535: Unregistered web context: /dcm4chee-web
		14:08:19,416 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-14) JBAS015877: Stopped deployment dcm4chee-arc-web-4.3.0.alpha4-mysql.war (runtime-name: dcm4chee-arc-web-4.3.0.alpha4-mysql.war) in 18ms
		..
		14:08:19,449 INFO  [org.jboss.as.server] (management-handler-thread - 16) JBAS018558: Undeployed "dcm4chee-arc-web-4.3.0.alpha4-mysql.war" (runtime-name: "dcm4chee-arc-web-4.3.0.alpha4-mysql.war")

Control DCM4CHEE Archive 4.x by HTTP GET
----------------------------------------

1.  `HTTP GET http://localhost:8080/dcm4chee-arc/ctrl/running` 
    returns `true`, if the archive is running, otherwise `false`.

2.  `HTTP GET http://localhost:8080/dcm4chee-arc/ctrl/stop` 
     stops DCM4CHEE Archive 4.x. 

3.  `HTTP GET http://localhost:8080/dcm4chee-arc/ctrl/start` 
    starts DCM4CHEE Archive 4.x. 

4.  `HTTP GET http://localhost:8080/dcm4chee-arc/ctrl/reload` 
    reloads the configuration from the configuration backend.

*Note*: `start`, `stop` and `reload` returns `HTTP status: 204 No Content` 
on success,  which causes some HTTP clients (in particular `wget`) to hang.


Testing DCM4CHEE Archive 4.x
----------------------------

For testing DCM4CHEE Archive 4.x, you may use DICOM and HL7 utilities provided by
[DCM4CHE 3.x](https://sourceforge.net/projects/dcm4che/files/dcm4che3/). After
extraction, you will find launcher scripts for Linux and Windows in directory 
`dcm4che-<version>/bin/`.


### Test Storage of DICOM Composite Objects

Use DCM4CHE 3.x's `storescu` utility to send DICOM Composite Objects to DCM4CHEE Archive 4.x, e.g.:

    > $DCM4CHE_HOME/bin/storescu -c DCM4CHEE@localhost:11112 ~/MESA-storage-A_12_5_0/modality/CT/CT1

Verify the success status=0H in logged `C-STORE-RSP` messages, e.g.:

    15:22:00,133 INFO  - DCM4CHEE-1 >> 1:C-STORE-RSP[pcid=3, status=0H
      cuid=1.2.840.10008.5.1.4.1.1.2 - CT Image Storage
      iuid=1.2.840.113674.950809132354242.100 - ?
      tsuid=1.2.840.10008.1.2 - Implicit VR Little Endian

You may try to send another set of objects via dicom-tls:

    > $DCM4CHE_HOME/bin/storescu -cDCM4CHEE@localhost:2762 --tls ~/MESA-storage-A_12_5_0/modality/CT/CT2


### Test Query for DICOM Composite Objects

Use DCM4CHE 3.x's `dcmdump` utility to determine the `Patient ID` of one of the
previous stored DICOM Composite Objects:

    > $DCM4CHE_HOME/bin/dcmdump ~/MESA-storage-A_12_5_0/modality/CT/CT1/CT1S1/CT1S1IM1.dcm | grep PatientID
    360: (0010,0020) LO #6 [GE0514] PatientID

and invoke a query for studies of this patient using DCM4CHE 3.x's `findscu` utility:

    > $DCM4CHE_HOME/bin/findscu -c DCM4CHEE@localhost:11112 \
      -mPatientID=GE0514 -rStudyInstanceUID

Verify, that there is at least one C-FIND-RSP with pending status=ff00H, e.g.:

    16:02:46,488 INFO  - DCM4CHEE-1 >> 1:C-FIND-RSP[pcid=1, status=ff00H
      cuid=1.2.840.10008.5.1.4.1.2.2.1 - Study Root Query/Retrieve Information Model - FIND
      tsuid=1.2.840.10008.1.2 - Implicit VR Little Endian
    16:02:46,489 DEBUG - Command:
    (0000,0002) UI [1.2.840.10008.5.1.4.1.2.2.1] AffectedSOPClassUID
    (0000,0100) US [32800] CommandField
    (0000,0120) US [1] MessageIDBeingRespondedTo
    (0000,0800) US [0] CommandDataSetType 06801411525
    (0000,0900) US [65280] Status

    16:02:46,489 DEBUG - Dataset:
    (0008,0052) CS [STUDY] QueryRetrieveLevel
    (0008,0054) AE [DCM4CHEE] RetrieveAETitle
    (0008,0056) CS [ONLINE] InstanceAvailability
    (0010,0020) LO [GE0514] PatientID
    (0020,000D) UI [1.2.840.113674.514.212.200] StudyInstanceUID

and that there is a final C-FIND-RSP with success status=0H, e.g.:

    16:02:46,492 INFO  - DCM4CHEE-1 >> 1:C-FIND-RSP[pcid=1, status=0H
      cuid=1.2.840.10008.5.1.4.1.2.2.1 - Study Root Query/Retrieve Information Model - FIND
      tsuid=1.2.840.10008.1.2 - Implicit VR Little Endian

### Test Retrieve of DICOM Composite Objects

Start DCM4CHE 3.x's `storescp` utility in a new console window for
acting as retrieve destination, listening on port 11115:

    > $DCM4CHE_HOME/bin/storescp -b11115
    16:28:46,910 INFO  - Start listening on 0.0.0.0/0.0.0.0:11115

Invoke a retrieve request for a study with given Study Instance UID
to STORESCP using DCM4CHE 3.x's `movescu` utility:

    > $DCM4CHE_HOME/bin/movescu -c DCM4CHEE@localhost:11112 --dest STORESCP \
      -mStudyInstanceUID=1.2.840.113674.514.212.200

Verify the success status=0H in the final `C-MOVE-RSP` messages, e.g.:

    16:40:13,698 INFO  - DCM4CHEE-1 >> 1:C-MOVE-RSP[pcid=1, completed=11, failed=0, warning=0, status=0H
      cuid=1.2.840.10008.5.1.4.1.2.2.2 - Study Root Query/Retrieve Information Model - MOVE
      tsuid=1.2.840.10008.1.2 - Implicit VR Little Endian
    16:40:13,698 DEBUG - Command:
    (0000,0002) UI [1.2.840.10008.5.1.4.1.2.2.2] AffectedSOPClassUID
    (0000,0100) US [32801] CommandField
    (0000,0120) US [1] MessageIDBeingRespondedTo
    (0000,0800) US [257] CommandDataSetType
    (0000,0900) US [0] Status
    (0000,1021) US [11] NumberOfCompletedSuboperations
    (0000,1022) US [0] NumberOfFailedSuboperations
    (0000,1023) US [0] NumberOfWarningSuboperations

and that `storescp` actually received the objects of the study, e.g.:

    16:40:13,687 INFO  - DCM4CHEE+1 >> 11:C-STORE-RQ[pcid=1, prior=0
      orig=MOVESCU >> 1:C-MOVE-RQ
      cuid=1.2.840.10008.5.1.4.1.1.2 - CT Image Storage
      iuid=1.2.840.113674.950809132339103.100 - ?
      tsuid=1.2.840.10008.1.2 - Implicit VR Little Endian
    16:40:13,688 DEBUG - Command:
    (0000,0002) UI [1.2.840.10008.5.1.4.1.1.2] AffectedSOPClassUID
    (0000,0100) US [1] CommandField
    (0000,0110) US [11] MessageID
    (0000,0700) US [0] Priority
    (0000,0800) US [0] CommandDataSetType
    (0000,1000) UI [1.2.840.113674.950809132339103.100] AffectedSOPInstanceUID
    (0000,1030) AE [MOVESCU] MoveOriginatorApplicationEntityTitle
    (0000,1031) US [1] MoveOriginatorMessageID

You may also try to retrieve the same study by C-GET instead of C-MOVE, using
DCM4CHE 3.x's `getscu` utility:

    > $DCM4CHE_HOME/bin/getscu -c DCM4CHEE@localhost:11112 \
      -mStudyInstanceUID=1.2.840.113674.514.212.200

### Test Web Access to DICOM Persistent Objects (WADO)
Use DCM4CHE 3.x's `dcmdump` utility to determine `Study`, `Series` and
'SOP Instance UID` of one of the previous stored DICOM Composite Objects:

    > $DCM4CHE_HOME/bin/dcmdump ~/MESA-storage-A_12_5_0/modality/CT/CT1/CT1S1/CT1S1IM1.dcm | grep InstanceUID
    68: (0008,0018) UI #34 [1.2.840.113674.950809132354242.100] SOPInstanceUID
    530: (0020,000D) UI #26 [1.2.840.113674.514.212.200] StudyInstanceUID
    564: (0020,000E) UI #30 [1.2.840.113674.514.212.81.300] SeriesInstanceUID

Invoke

    GET http://localhost:8080/dcm4chee-arc/rs/wado/DCM4CHEE?requestType=WADO
      &studyUID=1.2.840.113674.514.212.200
      &seriesUID=1.2.840.113674.514.212.81.300
      &objectUID=1.2.840.113674.950809132354242.100
      &contentType=application/dicom

by your Web Browser or any other HTTP client to retrieve the DICOM object.

Invoke

    GET http://localhost:8080/dcm4chee-arc/rs/wado/DCM4CHEE/studies/1.2.840.113674.514.212.200

by your Web Browser or any other HTTP client to retrieve all DICOM objects of
the Study in a ZIP Archive.


### Test [Query based on ID for DICOM Objects by RESTful Services (QIDO-RS)][1]

Open the Web interface (http://localhost:8080/dcm4chee-web) in your Web Browser.
Verify that the `AE Title` field in the Web interface matches the configured
AE Title of the archive (default: `DCM4CHEE`).

The maximal number of returned matches is configurable by the `Limit' field
(default: 20).

Click on S(earch Studies) to invoke a QIDO-RS `searchForStudies` HTTP GET request.
Received JSON Objects for each matching study will show up as rows in the table.

Click on S(earch Series) in Study rows invoke a QIDO-RS `searchForSeries`
HTTP GET request. Received JSON Objects for Series of the Study will show
up in inserted rows in the table.

Click on S(earch Instances) in Series rows invoke a QIDO-RS `searchForInstances`
HTTP GET request. Received JSON Objects for Instances of the Series will show
up in inserted rows in the table.

The maximal number of returned matches is configurable by the `Limit' field
(default: 20), which is also effective for shown Series and Instances. You may
use `>` to request following objects. 


### Test [Store Over the Web by RESTful Services (STOW-RS)][2]

Open the Web interface (http://localhost:8080/dcm4chee-web) in your Web Browser
and switch to the `Upload` tab. Verify that the `AE Title` field in the Web
interface matches the configured AE Title of the archive (default: `DCM4CHEE`).

Drop DICOM files in the drop box or click on it to open a File Browser to
select multiple DICOM files to be stored in the archive.

Click `Start Upload` to invoke a STOW-RS `Store Instances` HTTP POST multipart
request including all selected DICOM objects to the archive.

[1]: ftp://medical.nema.org/medical/dicom/supps/LB/sup166_lb.pdf
[2]: ftp://medical.nema.org/medical/dicom/Final/sup163_ft3.pdf

