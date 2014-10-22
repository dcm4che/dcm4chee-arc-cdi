package org.dcm4chee.archive.datamgmt.impl;

import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.dcm4chee.archive.datamgmt.QCEvent;
import org.dcm4chee.archive.datamgmt.QCNotification;
import org.slf4j.LoggerFactory;

public class QCPostProcessor {

    static final Logger LOG = LoggerFactory.getLogger(QCPostProcessor.class);
    public void observeQC(@Observes @QCNotification QCEvent event) {
        LOG.info("QC operation successfull, starting post processing");
    }
}
