package org.dcm4chee.archive.query.impl;

import static org.dcm4chee.archive.entity.Availability.NEARLINE;
import static org.dcm4chee.archive.entity.Availability.ONLINE;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.entity.Availability;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.impl.QueryServiceEJB.CommonStudySeriesQueryAttributesFactory;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mysema.query.Tuple;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Utils.class, StringUtils.class })
public class CommonStudySeriesQueryAttributesFactoryTest {
    private static final String[] EXISTING_RETRIEVE_AETS = { "existing retrieve aets" };

    private static final String RETURNED_EXTERNAL_RETRIEVE_AET = "returned external retrieve aet";

    private static final String RETURNED_RETRIEVE_AETS = "returned retrieve aets";

    private static final String[] RETURNED_RETRIEVE_AETS_ARRAY = { RETURNED_RETRIEVE_AETS };

    private static final String[] RETRIEVE_AETS_INTERSECTION = { "retrieve aets intersection" };

    EasyMockSupport easyMockSupport;

    CommonStudySeriesQueryAttributesFactory cut;

    @Before
    public void before() {
        PowerMock.mockStatic(StringUtils.class);
        PowerMock.mockStatic(Utils.class);
        
        easyMockSupport = new EasyMockSupport();

        cut = easyMockSupport.createMockBuilder(
                CommonStudySeriesQueryAttributesFactory.class).createMock();
    }

    Tuple createMockTuple(String retrieveAets, String[] retrieveAetsArray,
            String externalRetrieveAet, Availability availability) {
        Tuple mockTuple = easyMockSupport.createMock(Tuple.class);

        expect(mockTuple.get(QInstance.instance.retrieveAETs)).andReturn(
                retrieveAets);
        expect(StringUtils.split(retrieveAets, '\\')).andReturn(
                retrieveAetsArray);
        expect(mockTuple.get(QInstance.instance.externalRetrieveAET))
                .andReturn(externalRetrieveAet);
        expect(mockTuple.get(QInstance.instance.availability)).andReturn(
                availability);

        return mockTuple;
    }

    @Test
    public void addInstance_shouldUseRetrievedValuesAsIs_whenNumberOfInstancesIsGreaterThanZero() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, RETURNED_EXTERNAL_RETRIEVE_AET,
                NEARLINE);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 0;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldIntersectRetrieveAets_whenNumberOfInstancesIsOne() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, RETURNED_EXTERNAL_RETRIEVE_AET,
                NEARLINE);

        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.externalRetrieveAET = RETURNED_EXTERNAL_RETRIEVE_AET;
        cut.availability = NEARLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 1;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetTheMinimumAvailability_whenNumberOfInstancesIsOne() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, RETURNED_EXTERNAL_RETRIEVE_AET,
                NEARLINE);

        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.externalRetrieveAET = RETURNED_EXTERNAL_RETRIEVE_AET;
        cut.availability = ONLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 1;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetExternalRetrieveAETToNull_whenTWOTuplesHaveDifferentExternalRetrieveAETs() {
        Tuple[] mockTuples = {
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, "A", NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, "B", NEARLINE) };

        expect(
                Utils.intersection(RETURNED_RETRIEVE_AETS_ARRAY,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETURNED_RETRIEVE_AETS_ARRAY);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.addInstance(mockTuples[0]);
        cut.addInstance(mockTuples[1]);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.externalRetrieveAET, is(nullValue()));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetExternalRetrieveAETToNull_whenTHREETuplesHaveDifferentExternalRetrieveAETs() {
        Tuple[] mockTuples = {
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, "A", NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, "B", NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, "C", NEARLINE) };

        expect(
                Utils.intersection(RETURNED_RETRIEVE_AETS_ARRAY,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETURNED_RETRIEVE_AETS_ARRAY).times(2);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.addInstance(mockTuples[0]);
        cut.addInstance(mockTuples[1]);
        cut.addInstance(mockTuples[2]);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.externalRetrieveAET, is(nullValue()));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
}