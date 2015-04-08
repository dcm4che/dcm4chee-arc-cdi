package org.dcm4chee.archive.query.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.impl.QueryServiceEJB.CommonStudySeriesQueryAttributesBuilder;
import org.dcm4chee.storage.conf.Availability;
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
public class CommonStudySeriesQueryAttributesBuilderTest {
    private static final String[] EXISTING_RETRIEVE_AETS = { "existing retrieve aets" };

    private static final String RETURNED_RETRIEVE_AETS = "returned retrieve aets";

    private static final String[] RETURNED_RETRIEVE_AETS_ARRAY = { RETURNED_RETRIEVE_AETS };

    private static final String[] RETRIEVE_AETS_INTERSECTION = { "retrieve aets intersection" };

    EasyMockSupport easyMockSupport;

    CommonStudySeriesQueryAttributesBuilder cut;

    @Before
    public void before() {
        PowerMock.mockStatic(StringUtils.class);
        PowerMock.mockStatic(Utils.class);
        
        easyMockSupport = new EasyMockSupport();

        cut = easyMockSupport.createMockBuilder(
                CommonStudySeriesQueryAttributesBuilder.class).createMock();
    }

    Tuple createMockTuple(String retrieveAets, String[] retrieveAetsArray, Availability availability) {
        Tuple mockTuple = easyMockSupport.createMock(Tuple.class);

        expect(mockTuple.get(QInstance.instance.retrieveAETs)).andReturn(
                retrieveAets);
        expect(StringUtils.split(retrieveAets, '\\')).andReturn(
                retrieveAetsArray);
        expect(mockTuple.get(QInstance.instance.availability)).andReturn(
                availability);

        return mockTuple;
    }

    @Test
    public void addInstance_shouldUseRetrievedValuesAsIs_whenNumberOfInstancesIsGreaterThanZero() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 0;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.availability, is(Availability.NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldIntersectRetrieveAets_whenNumberOfInstancesIsOne() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE);

        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.availability = Availability.NEARLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 1;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.availability, is(Availability.NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetTheMinimumAvailability_whenNumberOfInstancesIsOne() {
        Tuple mockTuple = createMockTuple(RETURNED_RETRIEVE_AETS,
                RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE);

        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.availability = Availability.ONLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.numberOfInstances = 1;
        cut.addInstance(mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.availability, is(Availability.NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetExternalRetrieveAETToNull_whenTWOTuplesHaveDifferentExternalRetrieveAETs() {
        Tuple[] mockTuples = {
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE) };

        expect(
                Utils.intersection(RETURNED_RETRIEVE_AETS_ARRAY,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETURNED_RETRIEVE_AETS_ARRAY);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.addInstance(mockTuples[0]);
        cut.addInstance(mockTuples[1]);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.availability, is(Availability.NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void addInstance_shouldSetExternalRetrieveAETToNull_whenTHREETuplesHaveDifferentExternalRetrieveAETs() {
        Tuple[] mockTuples = {
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE),
                createMockTuple(RETURNED_RETRIEVE_AETS, RETURNED_RETRIEVE_AETS_ARRAY, Availability.NEARLINE) };

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
        assertThat(cut.availability, is(Availability.NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
}