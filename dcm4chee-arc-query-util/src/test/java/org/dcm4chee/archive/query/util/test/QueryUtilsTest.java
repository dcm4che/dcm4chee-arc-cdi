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

package org.dcm4chee.archive.query.util.test;

import static org.dcm4chee.archive.entity.Study.FIND_BY_STUDY_INSTANCE_UID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.entity.QPatient;
import org.dcm4chee.archive.entity.QStudy;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.query.util.*;
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

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.DateTimePath;
/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */

public class QueryUtilsTest {
    public static final EntityManagerFactoryRule ENTITY_MANAGER_FACTORY_RULE = new EntityManagerFactoryRule(
            "query-test");
    public static final EntityManagerRule ENTITY_MANAGER_RULE = new EntityManagerRule(
            ENTITY_MANAGER_FACTORY_RULE);

    public static final IDatabaseConnectionRule DATABASE_CONNECTION_RULE = new IDatabaseConnectionRule(
            ENTITY_MANAGER_RULE);

    @ClassRule
    public static TestRule TEST_RULE = RuleChain
            .outerRule(ENTITY_MANAGER_FACTORY_RULE).around(ENTITY_MANAGER_RULE)
            .around(DATABASE_CONNECTION_RULE);

    @ClassRule
    public static final IDatasetRule DATASET_RULE = new IDatasetRule(
            "query-dataset.xml");

    static IDatabaseConnection iDatabaseConnection;

    static IDataSet iDataSet;

    static EntityManager entityManager;

    @BeforeClass
    public static void beforeClass() throws Exception {
        iDataSet = DATASET_RULE.getiDataSet();
        entityManager = ENTITY_MANAGER_RULE.getEntityManager();
        iDatabaseConnection = DATABASE_CONNECTION_RULE
                .getiDatabaseConnection();
    }

    @Before
    public void before() throws Exception {
        DatabaseOperation.CLEAN_INSERT.execute(iDatabaseConnection, iDataSet);
    }

    /***********************
     * Date Only Test Cases
     **********************/

    @Test
    public void matchDateOnlyTestExactDateMatch() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151007";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchDateOnlyTestOpenEndedIntervalOutOfRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151008-";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(0));
        
    }

    @Test
    public void matchDateOnlyTestOpenEndedIntervalRangeBefore() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151006-";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchDateOnlyTestOpenEndedIntervalRangeInclusive() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151007-";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchDateOnlyTestClosedEndedIntervalRangeInclusive() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151006-20151008";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(1));
    }


    @Test
    public void matchDateOnlyTestClosedEndedIntervalOutOfRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String daString = "20151008-20151009";
        List<Study> studies = createDateRangeQuery(testAttrs, daString);
        assertThat(studies.size(), is(0));
    }

    /***********************
     * Time Only Test Cases
     **********************/

    @Test
    public void matchTimeOnlyTestExactTimeMatch() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121212.000";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchTimeOnlyTestOpenEndedIntervalOutOfRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121213.000-";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(0));
        
    }

    @Test
    public void matchTimeOnlyTestOpenEndedIntervalRangeBefore() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121211.000-";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchTimeOnlyTestOpenEndedIntervalRangeInclusive() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121212.000-";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchTimeOnlyTestClosedEndedIntervalRangeInclusive() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121212.000-121212.999";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(1));
    }


    @Test
    public void matchTimeOnlyTestClosedEndedIntervalOutOfRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121210.000-121211.000";
        List<Study> studies = createTimeRangeQuery(testAttrs, tmString);
        assertThat(studies.size(), is(0));
    }

    /***********************
     * Date Time Test Cases
     **********************/

    @Test
    public void matchDateTimeTestClosedEndedIntervalOutOfRangeNoCombined() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121213.000-121213.999";
        String daString = "20151007-20151009";
        List<Study> studies = createDateTimeRangeQuery(testAttrs, tmString, daString, false);
        assertThat(studies.size(), is(0));
    }

    @Test
    public void matchDateTimeTestClosedEndedIntervalOutOfRangeCombined() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121213.000-121213.999";
        String daString = "20151006-20151009";
        List<Study> studies = createDateTimeRangeQuery(testAttrs, tmString, daString, true);
        assertThat(studies.size(), is(1));
    }

    @Test
    public void matchDateTimeTestClosedEndedIntervalNoCombined() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Attributes testAttrs = new Attributes();
        String tmString = "121210-121212";
        String daString = "20151007-20151009";
        List<Study> studies = createDateTimeRangeQuery(testAttrs, tmString, daString, false);
        assertThat(studies.size(), is(1));
    }

    private List<Study> createDateRangeQuery(Attributes testAttrs,
            String daString) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        BooleanBuilder builder = new BooleanBuilder();
        Method rangeMatchMethod = MatchDateTimeRange.class.getDeclaredMethod("rangeMatch", DateTimePath.class,
                int.class, int.class, long.class, Attributes.class, boolean.class, boolean.class);
        rangeMatchMethod.setAccessible(true);
        testAttrs.setString(Tag.StudyDate, VR.DA, daString );
        List<Study> studies = applyRangedMatch(testAttrs, builder, rangeMatchMethod, false);
        return studies;
    }

    private List<Study> createTimeRangeQuery(Attributes testAttrs,
            String tmString) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        BooleanBuilder builder = new BooleanBuilder();
        Method rangeMatchMethod = MatchDateTimeRange.class.getDeclaredMethod("rangeMatch", DateTimePath.class,
                int.class, int.class, long.class, Attributes.class, boolean.class, boolean.class);
        rangeMatchMethod.setAccessible(true);
        testAttrs.setString(Tag.StudyTime, VR.TM, tmString );
        List<Study> studies = applyRangedMatch(testAttrs, builder, rangeMatchMethod, false);
        return studies;
    }

    private List<Study> createDateTimeRangeQuery(Attributes testAttrs,
            String tmString, String daString, boolean combinedDateTime) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException {
        BooleanBuilder builder = new BooleanBuilder();
        Method rangeMatchMethod = MatchDateTimeRange.class.getDeclaredMethod("rangeMatch", DateTimePath.class,
                int.class, int.class, long.class, Attributes.class, boolean.class, boolean.class);
        rangeMatchMethod.setAccessible(true);
        testAttrs.setString(Tag.StudyDate, VR.DA, daString );
        testAttrs.setString(Tag.StudyTime, VR.TM, tmString );
        List<Study> studies = applyRangedMatch(testAttrs, builder, rangeMatchMethod, combinedDateTime);
        return studies;
    }

    private List<Study> applyRangedMatch(Attributes testAttrs, BooleanBuilder builder, Method rangeMatchMethod, boolean combined)
            throws IllegalAccessException, InvocationTargetException {
        builder.and((Predicate) rangeMatchMethod.invoke(null, QStudy.study.studyDateTime, Tag.StudyDate, Tag.StudyTime,
                Tag.StudyDateAndTime, testAttrs, combined, false));
        JPAQuery queryWillMatchExact = new JPAQuery(entityManager).from(QStudy.study);

        queryWillMatchExact = queryWillMatchExact.innerJoin(QStudy.study.patient, QPatient.patient);
        queryWillMatchExact = queryWillMatchExact.where(builder);
        
        List<Study> studies = queryWillMatchExact.list(QStudy.study);
        return studies;
    }
}
