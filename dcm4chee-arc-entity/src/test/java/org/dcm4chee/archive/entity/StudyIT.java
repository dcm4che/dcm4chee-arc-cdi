/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.entity;

import static org.dcm4chee.archive.entity.Study.FIND_BY_STUDY_INSTANCE_UID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.dcm4chee.archive.junit.rules.EntityManagerFactoryRule;
import org.dcm4chee.archive.junit.rules.EntityManagerRule;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class StudyIT {
    public static final EntityManagerFactoryRule ENTITY_MANAGER_FACTORY_RULE = new EntityManagerFactoryRule(
            "study-it");

    public static final EntityManagerRule ENTITY_MANAGER_RULE = new EntityManagerRule(
            ENTITY_MANAGER_FACTORY_RULE);

    @ClassRule
    public static TestRule TEST_RULE = RuleChain.outerRule(
            ENTITY_MANAGER_FACTORY_RULE).around(ENTITY_MANAGER_RULE);

    static IDatabaseConnection iDatabaseConnection;

    static IDataSet iDataSet;

    static EntityManager entityManager;

    @BeforeClass
    public static void beforeClass() throws Exception {
        FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);

        iDataSet = flatXmlDataSetBuilder.build(Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("study-it-dataset.xml"));

        entityManager = ENTITY_MANAGER_RULE.getEntityManager();
        ((Session) entityManager.unwrap(Session.class)).doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {
                try {
                    iDatabaseConnection = new DatabaseConnection(connection);
                } catch (DatabaseUnitException e) {
                    throw new IllegalStateException(e);
                }
            }
        });

        iDatabaseConnection.getConfig().setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new HsqldbDataTypeFactory());
    }

    @Before
    public void before1() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(iDatabaseConnection, iDataSet);
    }

    // @Test
    // public void test() throws Exception {
    // // database connection
    // Class driverClass = Class.forName("oracle.jdbc.OracleDriver");
    // Connection jdbcConnection = DriverManager.getConnection(
    // "jdbc:oracle:thin:@idc-database.mitra.com:1521:impax", "crcsp",
    // "crcsp");
    // IDatabaseConnection connection = new
    // DatabaseConnection(jdbcConnection);
    //
    // // partial database export
    // QueryDataSet partialDataSet = new QueryDataSet(connection);
    // partialDataSet.addTable("STUDY");
    // FlatXmlDataSet.write(partialDataSet, new
    // FileOutputStream("study-it-dataset.xml"));
    //
    // // full database export
    // IDataSet fullDataSet = connection.createDataSet();
    // FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full.xml"));
    //
    // // dependent tables database export: export table X and all tables
    // that
    // // have a PK which is a FK on X, in the right order for insertion
    // String[] depTableNames =
    // TablesDependencyHelper.getAllDependentTables( connection, "X" );
    // IDataSet depDataset = connection.createDataSet( depTableNames );
    // FlatXmlDataSet.write(depDataSet, new
    // FileOutputStream("dependents.xml"));
    // *
    // }

    @Test
    public void findByStudyInstanceUID_shouldSelectStudy_whenStudyInstanceUIDMatches() {
        Study study = (Study) entityManager
                .createNamedQuery(FIND_BY_STUDY_INSTANCE_UID)
                .setParameter(1, "1").getSingleResult();

        assertThat(study.getPk(), is(1L));
    }

    @Test
    public void updateSeries1_shouldSeries1_whenPkMatches() throws Exception {
        assertThat(executeInTransaction(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Integer integer = entityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_SERIES[0])
                        .setParameter(1, 7).setParameter(2, 2L).executeUpdate();

                assertSeriesInstanceCounts(entityManager.find(Study.class, 1L),
                        110, 120, 130);
                assertSeriesInstanceCounts(entityManager.find(Study.class, 2L),
                        7, 220, 230);

                return integer;
            }
        }), is(1));
    }

    @Test
    public void updateSeries2_shouldSeries2_whenPkMatches() throws Exception {
        assertThat(executeInTransaction(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Integer integer = entityManager
                        .createNamedQuery(Study.UPDATE_NUMBER_OF_SERIES[1])
                        .setParameter(1, 7).setParameter(2, 2L).executeUpdate();

                assertSeriesInstanceCounts(entityManager.find(Study.class, 1L),
                        110, 120, 130);
                assertSeriesInstanceCounts(entityManager.find(Study.class, 2L),
                        210, 7, 230);

                return integer;
            }
        }), is(1));
    }

    private void assertSeriesInstanceCounts(Study study, int... seriesCounts) {
        entityManager.refresh(study);

        assertThat(study.getNumberOfSeries(1), is(seriesCounts[0]));
        assertThat(study.getNumberOfSeries(2), is(seriesCounts[1]));
        assertThat(study.getNumberOfSeries(3), is(seriesCounts[2]));
    }

    private <T> T executeInTransaction(Callable<T> callable) throws Exception {
        EntityTransaction entityTransaction = entityManager.getTransaction();

        try {
            entityTransaction.begin();

            T t = callable.call();

            entityTransaction.commit();

            return t;
        } catch (RuntimeException e) {
            if (entityTransaction != null && entityTransaction.isActive()) {
                entityTransaction.rollback();
            }

            throw e;
        }
    }
}
