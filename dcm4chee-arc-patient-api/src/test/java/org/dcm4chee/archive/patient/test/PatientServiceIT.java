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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4chee.archive.patient.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.IDWithIssuer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.soundex.ESoundex;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;
import org.dcm4chee.archive.conf.StoreParam;
import org.dcm4chee.archive.entity.BlobCorruptedException;
import org.dcm4chee.archive.entity.Issuer;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PatientID;
import org.dcm4chee.archive.entity.PersonName;
import org.dcm4chee.archive.patient.IDPatientSelector;
import org.dcm4chee.archive.patient.NonUniquePatientException;
import org.dcm4chee.archive.patient.PatientCircularMergedException;
import org.dcm4chee.archive.patient.PatientMergedException;
import org.dcm4chee.archive.patient.PatientService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@RunWith(Arquillian.class)
public class PatientServiceIT {

    @Inject
    private PatientService service;

    @PersistenceContext
    EntityManager em;

    @Resource
    UserTransaction utx;

    /**
     * Creates the deployment.
     * 
     * @return the web archive
     */
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
        war.addClass(PatientServiceIT.class);
        JavaArchive[] archs = Maven.resolver().loadPomFromFile("testpom.xml")
                .importRuntimeAndTestDependencies().resolve()
                .withTransitivity().as(JavaArchive.class);
        war.addAsLibraries(archs);

        return war;
    }

    /**
     * Setup.
     * 
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
     */
    @Before
    public void setup() throws NotSupportedException, SystemException {
        utx.begin();
        em.joinTransaction();
    }

    /**
     * Finalize test.
     * 
     * @throws SecurityException
     *             the security exception
     * @throws IllegalStateException
     *             the illegal state exception
     * @throws RollbackException
     *             the rollback exception
     * @throws HeuristicMixedException
     *             the heuristic mixed exception
     * @throws HeuristicRollbackException
     *             the heuristic rollback exception
     * @throws SystemException
     *             the system exception
     */
    @After
    public void finalizeTest() throws SecurityException, IllegalStateException,
            RollbackException, HeuristicMixedException,
            HeuristicRollbackException, SystemException {
        utx.commit();
        em.clear();
    }

    /**
     * Testupdate or create patient on c store one patient created old name kept
     * merged i ds same issuer.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreOnePatientCreatedOldNameKeptMergedIDsSameIssuer()
            throws Exception {
        Patient[] patients = initupdateOrCreatePatientOnCStore(true, true,
                true, 1);
        Attributes attrs = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        Assert.assertTrue(attrs.getString(Tag.PatientName).contains(
                "Bugs^Bunny"));
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        String coll = "";
        String collBlob = "";
        for (int i = 0; i < patients[0].getPatientIDs().toArray().length; i++) {
            coll += ((PatientID) patients[0].getPatientIDs().toArray()[i])
                    .getID();
        }
        collBlob += "" + attrs.getString(Tag.PatientID);
        Sequence tmpSeq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        for (int i = 0; i < tmpSeq.size(); i++)
            collBlob += tmpSeq.get(i).getString(Tag.PatientID);

        for (int i = 0; i < lstID.size(); i++) {
            Assert.assertTrue(coll.contains(lstID.get(i).getID()));
            Assert.assertTrue(collBlob.contains(lstID.get(i).getID()));
        }
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    /**
     * Testupdate or create patient on c store one patient created old name kept
     * merged i ds same issuer representation.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreOnePatientCreatedOldNameKeptMergedIDsSameIssuerRepresentation()
            throws Exception {
        Patient[] patients = initupdateOrCreatePatientOnCStore(true, true,
                true, 2);
        Attributes attrs = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        Assert.assertTrue(attrs.getString(Tag.PatientName).contains(
                "Bugs^Bunny"));
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        checkIDs(patients, attrs, lstID);
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    private void checkIDs(Patient[] patients, Attributes attrs,
            LinkedList<PatientID> lstID) {
        String coll = "";
        String collBlob = "";
        for (int i = 0; i < patients[0].getPatientIDs().toArray().length; i++) {
            coll += ((PatientID) patients[0].getPatientIDs().toArray()[i])
                    .getID();
        }
        collBlob += "" + attrs.getString(Tag.PatientID);
        Sequence tmpSeq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        for (int i = 0; i < tmpSeq.size(); i++)
            collBlob += tmpSeq.get(i).getString(Tag.PatientID);

        for (int i = 0; i < lstID.size(); i++) {
            Assert.assertTrue(coll.contains(lstID.get(i).getID()));
            Assert.assertTrue(collBlob.contains(lstID.get(i).getID()));

        }
    }

    /**
     * Testupdate or create patient on c store one patient created old name kept
     * merged i ds same issuer first no issuer second full.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreOnePatientCreatedOldNameKeptMergedIDsSameIssuerFirstNoIssuerSecondFull()
            throws Exception {
        Patient[] patients = initupdateOrCreatePatientOnCStore(false, true,
                true, 3);
        Attributes attrs = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(((PatientID) patients[0].getPatientIDs().toArray()[0])
                .getIssuer().toString().contains("G12345&G12345&ISO"));
        Set<IDWithIssuer> ids = IDWithIssuer.pidsOf(attrs);
        Assert.assertTrue(((IDWithIssuer) ids.toArray()[0]).getIssuer()
                .toString().contains("G12345&G12345&ISO"));
        Assert.assertSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        checkIDs(patients, attrs, lstID);
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    /**
     * Testupdate or create patient on c store one patient created old name kept
     * merged i ds same issuer firstlocal issuer second full.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreOnePatientCreatedOldNameKeptMergedIDsSameIssuerFirstlocalIssuerSecondFull()
            throws Exception {

        Attributes patientOneAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Bugs^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "123");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G12345");
        Attributes patientTwoAttributes = new Attributes();
        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Lola^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "444");
        patientTwoAttributes = setIssuer(3, patientTwoAttributes, "G12345");

        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());

        Sequence otherPatientIDs = patientTwoAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 2);
        Attributes tempSeqItem1 = new Attributes();
        tempSeqItem1.setString(Tag.PatientID, VR.LO, "123");
        tempSeqItem1 = setIssuer(3, tempSeqItem1, "G12345");
        Attributes tempSeqItem2 = new Attributes();
        tempSeqItem2.setString(Tag.PatientID, VR.LO, "789");
        tempSeqItem2 = setIssuer(3, tempSeqItem2, "G12345");
        otherPatientIDs.add(tempSeqItem1);
        otherPatientIDs.add(tempSeqItem2);

        Patient patientTwo = service.updateOrCreatePatientOnCStore(
                patientTwoAttributes, new IDPatientSelector(),
                createStoreParam());

        Patient[] patients = { patientOne, patientTwo };
        Attributes attrs = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(((PatientID) patients[0].getPatientIDs().toArray()[0])
                .getIssuer().toString().contains("G12345&G12345&ISO"));
        Set<IDWithIssuer> ids = IDWithIssuer.pidsOf(attrs);
        Assert.assertTrue(((IDWithIssuer) ids.toArray()[0]).getIssuer()
                .toString().contains("G12345&G12345&ISO"));
        Assert.assertSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        checkIDs(patients, attrs, lstID);
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    /**
     * Cleanupdate or create patient on c store.
     * 
     * @param patients
     *            the patients
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
     * @throws SecurityException
     *             the security exception
     * @throws IllegalStateException
     *             the illegal state exception
     * @throws RollbackException
     *             the rollback exception
     * @throws HeuristicMixedException
     *             the heuristic mixed exception
     * @throws HeuristicRollbackException
     *             the heuristic rollback exception
     */
    private void cleanupdateOrCreatePatientOnCStore(Patient[] patients) {
        for (Patient pat : patients) {
            em.remove(pat);
            em.remove(pat.getPatientName());
        }
        em.flush();

        ArrayList<Long> issuersToRemovePKs = new ArrayList<Long>();
        for (Patient p : patients) {
            for (PatientID id : p.getPatientIDs()) {
                if (!issuersToRemovePKs.contains(id.getPk()))
                    issuersToRemovePKs.add(id.getIssuer().getPk());
            }
        }

        for (Long pk : issuersToRemovePKs) {
            Issuer tmpIssuer = em.find(Issuer.class, pk);
            if (tmpIssuer != null)
                em.remove(tmpIssuer);
        }
        em.flush();

    }

    /**
     * Testupdate or create patient on c store different issuers two created.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreDifferentIssuersTwoCreated()
            throws Exception {
        Patient[] patients = initupdateOrCreatePatientOnCStore(true, true,
                false, 1);
        Attributes attrs0 = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(attrs0.getString(Tag.PatientName).contains(
                "Bugs^Bunny"));
        Assert.assertTrue(attrs0.getString(Tag.PatientID).contains("123"));
        Attributes attrs1 = decodeAttributes(patients[1].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(attrs0.getString(Tag.PatientName).contains(
                "Bugs^Bunny"));
        Assert.assertTrue(attrs1.getString(Tag.PatientName).contains(
                "Lola^Bunny"));
        Assert.assertNotSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        PersonName pn1 = patients[1].getPatientName();
        Assert.assertEquals("Lola", pn1.getFamilyName());
        Assert.assertEquals("Bunny", pn1.getGivenName());
        Assert.assertEquals(
                ((PatientID) patients[0].getPatientIDs().toArray()[0]).getID(),
                "123");
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        String coll = ((PatientID) patients[1].getPatientIDs().toArray()[0])
                .getID()
                + ""
                + ((PatientID) patients[1].getPatientIDs().toArray()[1])
                        .getID()
                + ((PatientID) patients[1].getPatientIDs().toArray()[2])
                        .getID();
        for (int i = 0; i < lstID.size(); i++) {
            Assert.assertTrue(coll.contains(lstID.get(i).getID()));
            continue;
        }
        cleanupdateOrCreatePatientOnCStore(patients);

    }

    /**
     * Testupdate or create patient on c store different issuer representation
     * two created.
     * 
     * @throws Exception
     *             the exception
     */
    @Test
    @Transactional(TransactionMode.ROLLBACK)
    public void testupdateOrCreatePatientOnCStoreDifferentIssuerRepresentationTwoCreated()
            throws Exception {
        Patient[] patients = initupdateOrCreatePatientOnCStore(true, true,
                false, 2);
        Attributes attrs0 = decodeAttributes(patients[0].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(attrs0.getString(Tag.PatientName).contains(
                "Bugs^Bunny"));
        Assert.assertTrue(attrs0.getString(Tag.PatientID).contains("123"));
        Attributes attrs1 = decodeAttributes(patients[1].getAttributesBlob().getEncodedAttributes());
        Assert.assertTrue(attrs1.getString(Tag.PatientName).contains(
                "Lola^Bunny"));
        Assert.assertTrue(attrs1.getString(Tag.PatientID).contains("444"));
        Assert.assertNotSame(patients[0], patients[1]);
        PersonName pn = patients[0].getPatientName();
        Assert.assertEquals("Bugs", pn.getFamilyName());
        Assert.assertEquals("Bunny", pn.getGivenName());
        PersonName pn1 = patients[1].getPatientName();
        Assert.assertEquals("Lola", pn1.getFamilyName());
        Assert.assertEquals("Bunny", pn1.getGivenName());
        Assert.assertEquals(
                ((PatientID) patients[0].getPatientIDs().toArray()[0]).getID(),
                "123");
        LinkedList<PatientID> lstID = new LinkedList<PatientID>();
        PatientID id1 = new PatientID();
        id1.setID("123");
        PatientID id2 = new PatientID();
        id2.setID("444");
        PatientID id3 = new PatientID();
        id3.setID("789");
        lstID.add(id1);
        lstID.add(id2);
        lstID.add(id3);
        String coll = ((PatientID) patients[1].getPatientIDs().toArray()[0])
                .getID()
                + ""
                + ((PatientID) patients[1].getPatientIDs().toArray()[1])
                        .getID()
                + ((PatientID) patients[1].getPatientIDs().toArray()[2])
                        .getID();
        for (int i = 0; i < lstID.size(); i++) {
            Assert.assertTrue(coll.contains(lstID.get(i).getID()));
            continue;
        }
        cleanupdateOrCreatePatientOnCStore(patients);

    }

    /**
     * Test merge with existing patient.
     * 
     * @throws PatientCircularMergedException
     *             the patient circular merged exception
     * @throws NonUniquePatientException
     *             the non unique patient exception
     * @throws PatientMergedException
     *             the patient merged exception
     * @throws SecurityException
     *             the security exception
     * @throws IllegalStateException
     *             the illegal state exception
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
     * @throws RollbackException
     *             the rollback exception
     * @throws HeuristicMixedException
     *             the heuristic mixed exception
     * @throws HeuristicRollbackException
     *             the heuristic rollback exception
     */
    @Test
    public void testMergeWithExistingPatient()
            throws NonUniquePatientException, PatientMergedException, PatientCircularMergedException {

        Attributes patientOneAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Bugs^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "123");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G12345");
        Attributes patientTwoAttributes = new Attributes();
        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Lola^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "444");
        patientTwoAttributes = setIssuer(1, patientTwoAttributes, "G12345");
        Sequence otherPatientIDs1 = patientOneAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 2);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "888");
        tempSeqItem = setIssuer(1, tempSeqItem, "G12345");
        otherPatientIDs1.add(tempSeqItem);
        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());
        patientOneAttributes.remove(Tag.OtherPatientIDsSequence);
        Sequence otherPatientIDs2 = patientTwoAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 2);
        Attributes tempSeqItem1 = new Attributes();
        tempSeqItem1.setString(Tag.PatientID, VR.LO, "999");
        tempSeqItem1 = setIssuer(1, tempSeqItem1, "G12345");
        Attributes tempSeqItem2 = new Attributes();
        tempSeqItem2.setString(Tag.PatientID, VR.LO, "789");
        tempSeqItem2 = setIssuer(1, tempSeqItem2, "G12345");
        otherPatientIDs2.add(tempSeqItem1);
        otherPatientIDs2.add(tempSeqItem2);
        Patient patientTwo = service.updateOrCreatePatientOnCStore(
                patientTwoAttributes, new IDPatientSelector(),
                createStoreParam());
        Attributes attrs0 = decodeAttributes(patientOne.getAttributesBlob().getEncodedAttributes());
        Set<IDWithIssuer> priorIDs = IDWithIssuer.pidsOf(attrs0);
        priorIDs.remove(IDWithIssuer.pidOf(attrs0));
        service.mergePatientByHL7(patientTwoAttributes, patientOneAttributes,
                createStoreParam());
        Attributes attrs1 = decodeAttributes(patientTwo.getAttributesBlob().getEncodedAttributes());
        Set<IDWithIssuer> DominantIDs = IDWithIssuer.pidsOf(attrs1);
        for (Iterator<IDWithIssuer> iter = priorIDs.iterator(); iter.hasNext();) {
            IDWithIssuer id = iter.next();
            Assert.assertTrue(DominantIDs.contains(id));
        }
        Patient[] patients = { patientOne, patientTwo };
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkUnlinkPatientsSimplaCase()
            throws PatientCircularMergedException, NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        Patient[] patients = initLinkPatients(patientOneAttributes,
                patientTwoAttributes);
        // link with A,b <-> D
        service.linkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        // check for linked ids
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[1].getLinkedPatientIDs().contains(id));
        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs)
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));

        // unlink A,b <-> D
        service.unlinkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());
        // check no ids linked
        patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertFalse(patients[1].getLinkedPatientIDs().contains(id));
        patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs)
            Assert.assertFalse(patients[0].getLinkedPatientIDs().contains(id));
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkUnlinkPatientsUnknownIdentifier()
            throws PatientCircularMergedException, NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        Patient[] patients = initLinkPatients(patientOneAttributes,
                patientTwoAttributes);
        // add unknown identifier
        Sequence otherPatientIDs2 = patientTwoAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 1);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "444");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs2.add(tempSeqItem);
        // link with A , B -> D , E (with E unknown)
        service.linkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());
        // check linked ids and new one created and linked
        boolean addedNewIdentifier = false;
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[1].getLinkedPatientIDs().contains(id));
        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));
            if (id.getID().compareTo("444") == 0)
                addedNewIdentifier = true;
        }
        // extra check for newly created id
        Assert.assertTrue(addedNewIdentifier);

        // unlink with A , B, C -> D , E (with C unknown)
        Sequence tmp = patientOneAttributes
                .getSequence(Tag.OtherPatientIDsSequence);
        Attributes tmpItem = new Attributes();
        tmpItem.setString(Tag.PatientID, VR.LO, "666");
        tmpItem = setIssuer(1, tmpItem, "G111222");
        tmp.add(tmpItem);
        // unlink
        service.unlinkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        // check no ids linked and new one created for patient one (C)
        addedNewIdentifier = false;
        patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs) {
            Assert.assertFalse(patients[1].getLinkedPatientIDs().contains(id));
            if (id.getID().compareTo("666") == 0)
                addedNewIdentifier = true;
        }
        patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs)
            Assert.assertFalse(patients[0].getLinkedPatientIDs().contains(id));

        Assert.assertTrue(addedNewIdentifier);

        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkUnLinkPatientsnotReferencedIdentifierInLinkMessage()
            throws PatientCircularMergedException, NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        Patient[] patients = initLinkPatients(patientOneAttributes,
                patientTwoAttributes);
        // remove the other id so it becomes unreferenced in the link message
        patientOneAttributes.remove(Tag.OtherPatientIDs);
        // link with A <-> D (with patient one having B unreferenced in the
        // link)
        service.linkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());
        // check all links created
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[1].getLinkedPatientIDs().contains(id));

        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));
        }

        // unlink with A <-> D (with patient one having B unreferenced in the
        // link)
        service.unlinkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        // check all links are broken

        patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertFalse(patients[1].getLinkedPatientIDs().contains(id));
        patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertFalse(patients[0].getLinkedPatientIDs().contains(id));
        }

        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkPatientsnoPatientRecordforOneIdentifier()
            throws PatientCircularMergedException, NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Link^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "111");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G111222");
        Sequence otherPatientIDs1 = patientOneAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 1);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "222");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs1.add(tempSeqItem);
        // create patient One
        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());
        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Linked^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "333");
        patientTwoAttributes = setIssuer(1, patientTwoAttributes, "G111222");
        patientOne.setLinkedPatientIDs(new ArrayList<PatientID>());

        // link with A,B <-> D (with D referring to no patient record)
        service.linkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        Query query = em
                .createQuery("SELECT p.patient from PatientID p where p.id = ?1");
        query.setParameter(1, "333");
        Patient patientTwo = (Patient) query.getSingleResult();
        Patient[] patients = { patientOne, patientTwo };
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[1].getLinkedPatientIDs().contains(id));

        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));
        }

        // unlink is not applicable here since the unlink returns on not finding
        // patient records for the id
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkPatientsnoPatientRecordforAllIdentifiers()
            throws NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Link^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "111");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G111222");
        Sequence otherPatientIDs1 = patientOneAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 1);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "222");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs1.add(tempSeqItem);

        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Linked^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "333");
        patientTwoAttributes = setIssuer(1, patientTwoAttributes, "G111222");

        // link with A,B <-> D (with A,B and D referring to no patient record)
        service.linkPatient(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        Query query = em
                .createQuery("SELECT p.patient from PatientID p where p.id = ?1 or p.id= ?2");
        query.setParameter(1, "333");
        query.setParameter(2, "111");
        @SuppressWarnings("unchecked")
        List<Patient> rs = query.getResultList();
        Patient patientOne = rs.get(0);
        Patient patientTwo = rs.get(1);
        Patient[] patients = { patientOne, patientTwo };
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[1].getLinkedPatientIDs().contains(id));

        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));
        }
        // unlink is not applicable here since the unlink returns on not finding
        // patient records for the id
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testMergeLinkedPatients()
            throws PatientCircularMergedException, NonUniquePatientException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Link^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "111");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G111222");
        Sequence otherPatientIDs1 = patientOneAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 1);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "222");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs1.add(tempSeqItem);
        // create patient 1
        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());
        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Linked^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "333");
        patientTwoAttributes = setIssuer(1, patientTwoAttributes, "G111222");
        // create patient 2
        Patient patientTwo = service.updateOrCreatePatientOnCStore(
                patientTwoAttributes, new IDPatientSelector(),
                createStoreParam());
        Attributes patientThreeAttributes = new Attributes();
        patientThreeAttributes.setString(Tag.PatientName, VR.PN,
                "Linkedz^Bunny");
        patientThreeAttributes.setString(Tag.PatientID, VR.LO, "555");
        patientThreeAttributes = setIssuer(1, patientThreeAttributes, "G111222");

        // create patient 3
        Patient patientThree = service.updateOrCreatePatientOnCStore(
                patientThreeAttributes, new IDPatientSelector(),
                createStoreParam());

        // link 2 and 3
        service.linkPatient(patientTwoAttributes, patientThreeAttributes,
                createStoreParam());
        // merge 1 dominant and 2 prior
        service.mergePatientByHL7(patientOneAttributes, patientTwoAttributes,
                createStoreParam());

        // Assert.assertTrue(patientOne.getPatientIDs().contains(((List)patientTwo.getPatientIDs()).get(0)));

        // link with D <-> E then merge D with patient 1 with A,B as ids

        Patient[] patients = { patientOne, patientTwo, patientThree };
        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertTrue(patients[2].getLinkedPatientIDs().contains(id));

        Collection<PatientID> patThreeIDs = patients[2].getPatientIDs();
        for (PatientID id : patThreeIDs) {
            Assert.assertTrue(patients[0].getLinkedPatientIDs().contains(id));
        }
        cleanupdateOrCreatePatientOnCStore(patients);
    }

    @Test
    public void testLinkPatientsdifferentPatientRecords()
            throws PatientCircularMergedException, PatientMergedException {
        Attributes patientOneAttributes = new Attributes();
        Attributes patientTwoAttributes = new Attributes();
        Patient[] patients = initLinkPatients(patientOneAttributes,
                patientTwoAttributes);
        Attributes patientThreeAttributes = new Attributes();
        patientThreeAttributes.setString(Tag.PatientName, VR.PN,
                "Linkedz^Bunny");
        patientThreeAttributes.setString(Tag.PatientID, VR.LO, "555");
        patientThreeAttributes = setIssuer(1, patientThreeAttributes, "G111222");
        Patient patientThree = service.updateOrCreatePatientOnCStore(
                patientThreeAttributes, new IDPatientSelector(),
                createStoreParam());
        patientThree.setLinkedPatientIDs(new ArrayList<PatientID>());

        // add the id from patient three to the link attributes
        Sequence otherPatientIDs2 = patientTwoAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 1);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "555");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs2.add(tempSeqItem);
        // link with A,B <-> D,E (with E belonging to a third patient)

        NonUniquePatientException e = null;
        try {
            service.linkPatient(patientOneAttributes, patientTwoAttributes,
                    createStoreParam());
        } catch (NonUniquePatientException e1) {
            e = e1;
        }

        Assert.assertNotNull(e);

        Collection<PatientID> patOneIDs = patients[0].getPatientIDs();
        for (PatientID id : patOneIDs)
            Assert.assertFalse(patients[1].getLinkedPatientIDs().contains(id));

        Collection<PatientID> patTwoIDs = patients[1].getPatientIDs();
        for (PatientID id : patTwoIDs) {
            Assert.assertFalse(patients[0].getLinkedPatientIDs().contains(id));
        }

        Patient[] newPatients = new Patient[3];
        newPatients[0] = patients[0];
        newPatients[1] = patients[1];
        newPatients[2] = patientThree;
        cleanupdateOrCreatePatientOnCStore(newPatients);
    }

    private Patient[] initLinkPatients(Attributes patientOneAttributes,
            Attributes patientTwoAttributes)
            throws PatientCircularMergedException {
        // create patient one

        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Link^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "111");
        patientOneAttributes = setIssuer(1, patientOneAttributes, "G111222");
        Sequence otherPatientIDs1 = patientOneAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 2);
        Attributes tempSeqItem = new Attributes();
        tempSeqItem.setString(Tag.PatientID, VR.LO, "222");
        tempSeqItem = setIssuer(1, tempSeqItem, "G111222");
        otherPatientIDs1.add(tempSeqItem);
        // create patient two
        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());

        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Linked^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "333");
        patientTwoAttributes = setIssuer(1, patientTwoAttributes, "G111222");
        Patient patientTwo = service.updateOrCreatePatientOnCStore(
                patientTwoAttributes, new IDPatientSelector(),
                createStoreParam());
        patientOne.setLinkedPatientIDs(new ArrayList<PatientID>());
        patientTwo.setLinkedPatientIDs(new ArrayList<PatientID>());
        Patient[] result = { patientOne, patientTwo };
        return result;
    }

    /**
     * Initupdate or create patient on c store.
     * 
     * @param withIssuer1
     *            the with issuer1
     * @param withIssuer2
     *            the with issuer2
     * @param sameIssuer
     *            the same issuer
     * @param issuerRepresentation
     *            the issuer representation
     * @return the patient[]
     * @throws NotSupportedException
     *             the not supported exception
     * @throws SystemException
     *             the system exception
     * @throws PatientCircularMergedException
     *             the patient circular merged exception
     * @throws SecurityException
     *             the security exception
     * @throws IllegalStateException
     *             the illegal state exception
     * @throws RollbackException
     *             the rollback exception
     * @throws HeuristicMixedException
     *             the heuristic mixed exception
     * @throws HeuristicRollbackException
     *             the heuristic rollback exception
     */
    private Patient[] initupdateOrCreatePatientOnCStore(boolean withIssuer1,
            boolean withIssuer2, boolean sameIssuer, int issuerRepresentation)
            throws NotSupportedException, SystemException,
            PatientCircularMergedException, SecurityException,
            IllegalStateException, RollbackException, HeuristicMixedException,
            HeuristicRollbackException {
        Attributes patientOneAttributes = new Attributes();
        patientOneAttributes.setString(Tag.PatientName, VR.PN, "Bugs^Bunny");
        patientOneAttributes.setString(Tag.PatientID, VR.LO, "123");
        if (withIssuer1)
            patientOneAttributes = setIssuer(issuerRepresentation,
                    patientOneAttributes, "G12345");
        Attributes patientTwoAttributes = new Attributes();
        patientTwoAttributes.setString(Tag.PatientName, VR.PN, "Lola^Bunny");
        patientTwoAttributes.setString(Tag.PatientID, VR.LO, "444");
        if (withIssuer2)
            if (sameIssuer)
                patientTwoAttributes = setIssuer(issuerRepresentation,
                        patientTwoAttributes, "G12345");
            else
                patientTwoAttributes = setIssuer(issuerRepresentation,
                        patientTwoAttributes, "G123456");

        Patient patientOne = service.updateOrCreatePatientOnCStore(
                patientOneAttributes, new IDPatientSelector(),
                createStoreParam());

        Sequence otherPatientIDs = patientTwoAttributes.newSequence(
                Tag.OtherPatientIDsSequence, 2);
        Attributes tempSeqItem1 = new Attributes();
        tempSeqItem1.setString(Tag.PatientID, VR.LO, "123");
        if (withIssuer2)
            if (sameIssuer)
                tempSeqItem1 = setIssuer(issuerRepresentation, tempSeqItem1,
                        "G12345");
            else
                tempSeqItem1 = setIssuer(issuerRepresentation, tempSeqItem1,
                        "G123456");
        Attributes tempSeqItem2 = new Attributes();
        tempSeqItem2.setString(Tag.PatientID, VR.LO, "789");
        if (withIssuer2)
            if (sameIssuer)
                tempSeqItem2 = setIssuer(issuerRepresentation, tempSeqItem2,
                        "G12345");
            else
                tempSeqItem2 = setIssuer(issuerRepresentation, tempSeqItem2,
                        "G123456");
        otherPatientIDs.add(tempSeqItem1);
        otherPatientIDs.add(tempSeqItem2);

        Patient patientTwo = service.updateOrCreatePatientOnCStore(
                patientTwoAttributes, new IDPatientSelector(),
                createStoreParam());

        Patient[] patients = { patientOne, patientTwo };
        return patients;
    }

    /**
     * Sets the issuer.
     * 
     * @param issuerRepresentation
     *            the issuer representation
     * @param patientTwoAttributes
     *            the patient two attributes
     * @param issuer
     *            the issuer
     * @return the attributes
     */
    private Attributes setIssuer(int issuerRepresentation,
            Attributes patientTwoAttributes, String issuer) {
        if (issuerRepresentation == 1) {
            patientTwoAttributes
                    .setString(Tag.IssuerOfPatientID, VR.LO, issuer);
        } else if (issuerRepresentation == 2) {
            Sequence issuerSeq = patientTwoAttributes.newSequence(
                    Tag.IssuerOfPatientIDQualifiersSequence, 1);
            Attributes tempItem = new Attributes();
            tempItem.setString(Tag.UniversalEntityID, VR.UT, issuer);
            tempItem.setString(Tag.UniversalEntityIDType, VR.CS, "ISO");
            issuerSeq.add(tempItem);
        } else {
            patientTwoAttributes
                    .setString(Tag.IssuerOfPatientID, VR.LO, issuer);
            Sequence issuerSeq = patientTwoAttributes.newSequence(
                    Tag.IssuerOfPatientIDQualifiersSequence, 1);
            Attributes tempItem = new Attributes();
            tempItem.setString(Tag.UniversalEntityID, VR.UT, issuer);
            tempItem.setString(Tag.UniversalEntityIDType, VR.CS, "ISO");
            issuerSeq.add(tempItem);
        }

        return patientTwoAttributes;
    }

    /**
     * Creates the store param.
     * 
     * @return the store param
     */
    public static StoreParam createStoreParam() {
        StoreParam storeParam = new StoreParam();
        storeParam.setAttributeFilters(ATTRIBUTE_FILTERS);
        storeParam.setFuzzyStr(new ESoundex());
        return storeParam;
    }

    public static Attributes decodeAttributes(byte[] b) {
        if (b == null || b.length == 0)
            return new Attributes(0);
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        try {
            @SuppressWarnings("resource")
            DicomInputStream dis = new DicomInputStream(is);
            return dis.readDataset(-1, -1);
        } catch (IOException e) {
            throw new BlobCorruptedException(e);
        }
    }

    private static final int[] PATIENT_ATTRS = { Tag.SpecificCharacterSet,
            Tag.PatientName, Tag.PatientID, Tag.IssuerOfPatientID,
            Tag.IssuerOfPatientIDQualifiersSequence, Tag.PatientBirthDate,
            Tag.PatientBirthTime, Tag.PatientSex,
            Tag.PatientInsurancePlanCodeSequence,
            Tag.PatientPrimaryLanguageCodeSequence, Tag.OtherPatientNames,
            Tag.OtherPatientIDsSequence, Tag.PatientBirthName, Tag.PatientAge,
            Tag.PatientSize, Tag.PatientSizeCodeSequence, Tag.PatientWeight,
            Tag.PatientAddress, Tag.PatientMotherBirthName, Tag.MilitaryRank,
            Tag.BranchOfService, Tag.MedicalRecordLocator, Tag.MedicalAlerts,
            Tag.Allergies, Tag.CountryOfResidence, Tag.RegionOfResidence,
            Tag.PatientTelephoneNumbers, Tag.EthnicGroup, Tag.Occupation,
            Tag.SmokingStatus, Tag.AdditionalPatientHistory,
            Tag.PregnancyStatus, Tag.LastMenstrualDate,
            Tag.PatientReligiousPreference, Tag.PatientSpeciesDescription,
            Tag.PatientSpeciesCodeSequence, Tag.PatientSexNeutered,
            Tag.PatientBreedDescription, Tag.PatientBreedCodeSequence,
            Tag.BreedRegistrationSequence, Tag.ResponsiblePerson,
            Tag.ResponsiblePersonRole, Tag.ResponsibleOrganization,
            Tag.PatientComments, Tag.ClinicalTrialSponsorName,
            Tag.ClinicalTrialProtocolID, Tag.ClinicalTrialProtocolName,
            Tag.ClinicalTrialSiteID, Tag.ClinicalTrialSiteName,
            Tag.ClinicalTrialSubjectID, Tag.ClinicalTrialSubjectReadingID,
            Tag.PatientIdentityRemoved, Tag.DeidentificationMethod,
            Tag.DeidentificationMethodCodeSequence,
            Tag.ClinicalTrialProtocolEthicsCommitteeName,
            Tag.ClinicalTrialProtocolEthicsCommitteeApprovalNumber,
            Tag.SpecialNeeds, Tag.PertinentDocumentsSequence, Tag.PatientState,
            Tag.PatientClinicalTrialParticipationSequence,
            Tag.ConfidentialityConstraintOnPatientDataDescription };

    private static final AttributeFilter PATIENT_ATTR_FILTER = new AttributeFilter(
            PATIENT_ATTRS);

    public static final Map<Entity, AttributeFilter> ATTRIBUTE_FILTERS;

    static {
        ATTRIBUTE_FILTERS = new HashMap<>();
        ATTRIBUTE_FILTERS.put(Entity.Patient, new AttributeFilter(PATIENT_ATTRS));
    }
}
