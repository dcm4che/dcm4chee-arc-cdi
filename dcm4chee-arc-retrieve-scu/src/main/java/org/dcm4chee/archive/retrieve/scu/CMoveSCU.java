package org.dcm4chee.archive.retrieve.scu;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.pdu.AAssociateRQ;

public class CMoveSCU {

	private AAssociateRQ rq;
	private int priority;
	private ApplicationEntity ae;
	private ApplicationEntity remoteAE;
	private String destination;
	private Association as;
	

	public CMoveSCU(ApplicationEntity ae, ApplicationEntity remoteAE
			, String destinationAET) {
		 this.rq = new AAssociateRQ();
		 this.rq.setCalledAET(remoteAE.getAETitle());
		 this.rq.setCallingAET(ae.getAETitle());
         this.ae = ae;
         this.remoteAE = remoteAE;
         this.destination = destinationAET;
	}

	public Association open()
			throws IOException, InterruptedException,
			IncompatibleConnectionException, GeneralSecurityException {
		return (as = ae.connect(remoteAE, rq));
	}

	public void close() throws IOException, InterruptedException {
		if (as != null && as.isReadyForDataTransfer()) {
			as.waitForOutstandingRSP();
			as.release();
		}
	}

	public void cmove(Attributes keys, DimseRSPHandler handler)
			throws IOException, InterruptedException {
		as.cmove(UID.StudyRootQueryRetrieveInformationModelMOVE, priority,
				keys, null, destination, handler);
	}

	public AAssociateRQ getRq() {
		return rq;
	}

	public Association getAs() {
		return as;
	}

	public ApplicationEntity getRemoteAE() {
		return remoteAE;
	}

	public ApplicationEntity getAe() {
		return ae;
	}

}
