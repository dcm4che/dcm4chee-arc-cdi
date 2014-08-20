package org.dcm4chee.archive.query.impl;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;

public class SessionProducer {
	@PersistenceContext(unitName="dcm4chee-arc")
	EntityManager em;
	
	@Produces
	Session produceSession() {
		return em.unwrap(Session.class);
	}
}
