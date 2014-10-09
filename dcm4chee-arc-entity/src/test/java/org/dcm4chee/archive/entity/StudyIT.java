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

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dcm4chee.archive.junit.rules.EntityManagerFactoryRule;
import org.dcm4chee.archive.junit.rules.EntityManagerRule;
import org.dcm4chee.archive.junit.rules.IDatabaseConnectionRule;
import org.dcm4chee.archive.junit.rules.IDatasetRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class StudyIT {
    private static final int NEW_COUNT = 7;

    private static final int[] INITIAL_STUDY_1_SERIES_COUNTS = new int[] { 110,
            120, 130 };

    private static final int[] INITIAL_STUDY_2_INSTANCE_COUNTS = new int[] {
            201, 202, 203 };

    public static final EntityManagerFactoryRule ENTITY_MANAGER_FACTORY_RULE = new EntityManagerFactoryRule(
            "study-it");

    public static final EntityManagerRule ENTITY_MANAGER_RULE = new EntityManagerRule(
            ENTITY_MANAGER_FACTORY_RULE);

    public static final IDatabaseConnectionRule I_DATABASE_CONNECTION_RULE = new IDatabaseConnectionRule(
            ENTITY_MANAGER_RULE);

    @ClassRule
    public static TestRule TEST_RULE = RuleChain
            .outerRule(ENTITY_MANAGER_FACTORY_RULE).around(ENTITY_MANAGER_RULE)
            .around(I_DATABASE_CONNECTION_RULE);

    @ClassRule
    public static final IDatasetRule I_DATASET_RULE = new IDatasetRule(
            "study-it-dataset.xml");

    static IDatabaseConnection iDatabaseConnection;

    static IDataSet iDataSet;

    static EntityManager entityManager;

    @BeforeClass
    public static void beforeClass() throws Exception {
        iDataSet = I_DATASET_RULE.getiDataSet();
        entityManager = ENTITY_MANAGER_RULE.getEntityManager();
        iDatabaseConnection = I_DATABASE_CONNECTION_RULE
                .getiDatabaseConnection();
    }

    @Before
    public void before() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(iDatabaseConnection, iDataSet);
    }

    @Test
    public void findByStudyInstanceUID_shouldSelectStudy_whenStudyInstanceUIDMatches() {
        Study study = (Study) entityManager
                .createNamedQuery(FIND_BY_STUDY_INSTANCE_UID)
                .setParameter(1, "1").getSingleResult();

        assertThat(study.getPk(), is(1L));
    }

//    @Test
//    public void updateNumberOfSeries1_shouldUpdateNumberOfSeries1_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfSeriesCallable(2L, 0,
//                        NEW_COUNT)), is(1));
//
//        assertNumberOfSeries(entityManager.find(Study.class, 1L),
//                INITIAL_STUDY_1_SERIES_COUNTS);
//        assertNumberOfSeries(entityManager.find(Study.class, 2L), NEW_COUNT,
//                220, 230);
//    }
//
//    @Test
//    public void updateNumberOfSeries2_shouldUpdateNumberOfSeries2_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfSeriesCallable(2L, 1,
//                        NEW_COUNT)), is(1));
//
//        assertNumberOfSeries(entityManager.find(Study.class, 1L),
//                INITIAL_STUDY_1_SERIES_COUNTS);
//        assertNumberOfSeries(entityManager.find(Study.class, 2L), 210,
//                NEW_COUNT, 230);
//    }
//
//    @Test
//    public void updateNumberOfSeries3_shouldUpdateNumberOfSeries3_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfSeriesCallable(2L, 2,
//                        NEW_COUNT)), is(1));
//
//        assertNumberOfSeries(entityManager.find(Study.class, 1L),
//                INITIAL_STUDY_1_SERIES_COUNTS);
//        assertNumberOfSeries(entityManager.find(Study.class, 2L), 210, 220,
//                NEW_COUNT);
//    }
//
//    @Test
//    public void updateNumberOfInstances1_shouldUpdateNumberOfInstances1_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfInstancesCallable(1L,
//                        0, NEW_COUNT)), is(1));
//
//        assertNumberOfInstances(entityManager.find(Study.class, 1L), NEW_COUNT,
//                102, 103);
//        assertNumberOfInstances(entityManager.find(Study.class, 2L),
//                INITIAL_STUDY_2_INSTANCE_COUNTS);
//    }
//
//    @Test
//    public void updateNumberOfInstances2_shouldUpdateNumberOfInstances2_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfInstancesCallable(1L,
//                        1, NEW_COUNT)), is(1));
//
//        assertNumberOfInstances(entityManager.find(Study.class, 1L), 101,
//                NEW_COUNT, 103);
//        assertNumberOfInstances(entityManager.find(Study.class, 2L),
//                INITIAL_STUDY_2_INSTANCE_COUNTS);
//    }
//
//    @Test
//    public void updateNumberOfInstances3_shouldUpdateNumberOfInstances3_whenPkMatches()
//            throws Exception {
//        assertThat(
//                executeInTransaction(createUpdateNumberOfInstancesCallable(1L,
//                        2, NEW_COUNT)), is(1));
//
//        assertNumberOfInstances(entityManager.find(Study.class, 1L), 101, 102,
//                NEW_COUNT);
//        assertNumberOfInstances(entityManager.find(Study.class, 2L),
//                INITIAL_STUDY_2_INSTANCE_COUNTS);
//    }
//
//    private Callable<Integer> createUpdateNumberOfSeriesCallable(final long pk,
//            final int queryIndex, final int seriesCount) throws Exception {
//        return new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Integer integer = entityManager
//                        .createNamedQuery(
//                                Study.UPDATE_NUMBER_OF_SERIES[queryIndex])
//                        .setParameter(1, seriesCount).setParameter(2, pk)
//                        .executeUpdate();
//
//                return integer;
//            }
//        };
//    }
//
//    private Callable<Integer> createUpdateNumberOfInstancesCallable(
//            final long pk, final int queryIndex, final int instanceCount)
//            throws Exception {
//        return new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                Integer integer = entityManager
//                        .createNamedQuery(
//                                Study.UPDATE_NUMBER_OF_INSTANCES[queryIndex])
//                        .setParameter(1, instanceCount).setParameter(2, pk)
//                        .executeUpdate();
//
//                return integer;
//            }
//        };
//    }
//
//    private void assertNumberOfSeries(Study study, int... numberOfSeries) {
//        entityManager.refresh(study);
//
//        assertThat(study.getNumberOfSeries(1), is(numberOfSeries[0]));
//        assertThat(study.getNumberOfSeries(2), is(numberOfSeries[1]));
//        assertThat(study.getNumberOfSeries(3), is(numberOfSeries[2]));
//    }
//
//    private void assertNumberOfInstances(Study study, int... numberOfInstances) {
//        entityManager.refresh(study);
//
//        assertThat(study.getNumberOfInstances(1), is(numberOfInstances[0]));
//        assertThat(study.getNumberOfInstances(2), is(numberOfInstances[1]));
//        assertThat(study.getNumberOfInstances(3), is(numberOfInstances[2]));
//    }
//
//    private <T> T executeInTransaction(Callable<T> callable) throws Exception {
//        EntityTransaction entityTransaction = entityManager.getTransaction();
//
//        try {
//            entityTransaction.begin();
//
//            T t = callable.call();
//
//            entityTransaction.commit();
//
//            return t;
//        } catch (RuntimeException e) {
//            if (entityTransaction != null && entityTransaction.isActive()) {
//                entityTransaction.rollback();
//            }
//
//            throw e;
//        }
//    }
}
