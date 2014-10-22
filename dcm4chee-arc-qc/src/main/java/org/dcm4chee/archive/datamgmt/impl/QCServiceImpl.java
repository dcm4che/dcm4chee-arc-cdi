package org.dcm4chee.archive.datamgmt.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4chee.archive.datamgmt.QCBean;

@ApplicationScoped
public class QCServiceImpl {
    @Inject
    private QCBean datamanager; 
}
