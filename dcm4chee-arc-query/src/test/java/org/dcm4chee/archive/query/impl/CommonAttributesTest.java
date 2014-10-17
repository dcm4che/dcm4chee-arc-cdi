package org.dcm4chee.archive.query.impl;

import static org.dcm4chee.archive.entity.Availability.NEARLINE;
import static org.dcm4chee.archive.entity.Availability.ONLINE;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.dcm4che3.util.StringUtils;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.Utils;
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
public class CommonAttributesTest {
    private static final String[] EXISTING_RETRIEVE_AETS = { "existing retrieve aets" };

    private static final String RETURNED_EXTERNAL_RETRIEVE_AET = "returned external retrieve aet";

    private static final String RETURNED_RETRIEVE_AETS = "returned retrieve aets";

    private static final String[] RETURNED_RETRIEVE_AETS_ARRAY = { RETURNED_RETRIEVE_AETS };

    private static final String[] RETRIEVE_AETS_INTERSECTION = { "retrieve aets intersection" };

    EasyMockSupport easyMockSupport;

    Tuple mockTuple;

    QueryServiceEJB.CommonAttributes cut;

    @Before
    public void before() {
        easyMockSupport = new EasyMockSupport();
        mockTuple = easyMockSupport.createMock(Tuple.class);

        PowerMock.mockStatic(StringUtils.class);
        PowerMock.mockStatic(Utils.class);

        expect(mockTuple.get(QInstance.instance.retrieveAETs)).andReturn(
                RETURNED_RETRIEVE_AETS);
        expect(StringUtils.split(RETURNED_RETRIEVE_AETS, '\\')).andReturn(
                RETURNED_RETRIEVE_AETS_ARRAY);
        expect(mockTuple.get(QInstance.instance.externalRetrieveAET))
                .andReturn(RETURNED_EXTERNAL_RETRIEVE_AET);
        expect(mockTuple.get(QInstance.instance.availability)).andReturn(
                NEARLINE);

        cut = easyMockSupport.createMockBuilder(
                QueryServiceEJB.CommonAttributes.class).createMock();
    }

    @Test
    public void updateAttributes_shouldUseRetrievedValuesAsIs_whenNumberOfInstancesIsGreaterThanZero() {
        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.updateAttributes(0, mockTuple);

        assertThat(cut.retrieveAETs, is(RETURNED_RETRIEVE_AETS_ARRAY));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }

    @Test
    public void updateAttributes_shouldIntersectRetrieveAets_whenNumberOfInstancesIsOne() {
        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.externalRetrieveAET = RETURNED_EXTERNAL_RETRIEVE_AET;
        cut.availability = NEARLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.updateAttributes(1, mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
    

    @Test
    public void updateAttributes_shouldSetTheMinimumAvailability_whenNumberOfInstancesIsOne() {
        cut.retrieveAETs = EXISTING_RETRIEVE_AETS;
        cut.externalRetrieveAET = RETURNED_EXTERNAL_RETRIEVE_AET;
        cut.availability = ONLINE;

        expect(
                Utils.intersection(EXISTING_RETRIEVE_AETS,
                        RETURNED_RETRIEVE_AETS_ARRAY)).andReturn(
                RETRIEVE_AETS_INTERSECTION);

        easyMockSupport.replayAll();
        PowerMock.replayAll();

        cut.updateAttributes(1, mockTuple);

        assertThat(cut.retrieveAETs, is(RETRIEVE_AETS_INTERSECTION));
        assertThat(cut.externalRetrieveAET, is(RETURNED_EXTERNAL_RETRIEVE_AET));
        assertThat(cut.availability, is(NEARLINE));

        PowerMock.verifyAll();
        easyMockSupport.verifyAll();
    }
}