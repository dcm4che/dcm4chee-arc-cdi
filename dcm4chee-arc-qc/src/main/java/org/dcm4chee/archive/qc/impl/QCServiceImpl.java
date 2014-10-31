package org.dcm4chee.archive.qc.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4chee.archive.qc.QCBean;

@ApplicationScoped
public class QCServiceImpl {
    @Inject
    private QCBean datamanager; 
}
