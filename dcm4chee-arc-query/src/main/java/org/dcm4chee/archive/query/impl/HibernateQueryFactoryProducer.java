package org.dcm4chee.archive.query.impl;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.Session;

import com.mysema.query.jpa.hibernate.HibernateQueryFactory;

public class HibernateQueryFactoryProducer {
	@Inject
	Instance<Session> session;
	
	@Produces
	HibernateQueryFactory produceHibernateQueryFactory() {
		return new HibernateQueryFactory(session);
	}
}
