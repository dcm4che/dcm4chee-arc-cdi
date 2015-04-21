package org.dcm4chee.archive.dto;

import javax.enterprise.util.AnnotationLiteral;

public class ServiceQualifier extends AnnotationLiteral<Service> implements Service{

    private static final long serialVersionUID = -3993237958841244931L;

        private ServiceType service;

        public ServiceQualifier(ServiceType service) {
            this.service = service;
        }

        public ServiceType value() {
            return service;
        }

        }

