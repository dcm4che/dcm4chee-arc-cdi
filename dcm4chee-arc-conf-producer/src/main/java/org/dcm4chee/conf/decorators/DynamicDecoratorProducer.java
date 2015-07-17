package org.dcm4chee.conf.decorators;

import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.query.DerivedSeriesFields;
import org.dcm4chee.archive.query.DerivedStudyFields;
import org.dcm4chee.archive.query.QueryService;
import org.dcm4chee.archive.retrieve.RetrieveService;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.scu.CStoreSCUService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collection;

@ApplicationScoped
public class DynamicDecoratorProducer {
	
	@Inject 
	@DynamicDecorator
	Instance<DelegatingServiceImpl<StoreService>> dynamicStoreDecorators;
	
	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<MPPSService>> dynamicMPPSDecorators;
	
	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<QueryService>> dynamicQueryDecorators;
	
	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<RetrieveService>> dynamicRetrieveDecorators;
	
	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<CStoreSCUService>> dynamicCStoreSCUDecorators;

	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<DerivedStudyFields>> dynamicDerivedStudyFieldsDecorators;

	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<DerivedSeriesFields>> dynamicDerivedSeriesFieldsDecorators;


	@Inject
	private ServiceDecorator<StoreService> storeDecorators;
	
	@Inject
	private ServiceDecorator<MPPSService> mppsDecorators;
	
	@Inject
	private ServiceDecorator<QueryService> queryDecorators;
	
	@Inject
	private ServiceDecorator<RetrieveService> retrieveDecorators;
	
	@Inject
	private ServiceDecorator<CStoreSCUService> cstoreSCUDecorators;

	@Inject
	private ServiceDecorator<DerivedStudyFields> derivedStudyDecorators;

	@Inject
	private ServiceDecorator<DerivedSeriesFields> derivedSeriesDecorators;
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<StoreService>> getConfiguredStoreServiceDynamicDecorators() {
		return storeDecorators.getOrderedDecorators(dynamicStoreDecorators, StoreService.class.getName());
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<MPPSService>> getConfiguredMPPSServiceDynamicDecorators() {
		return mppsDecorators.getOrderedDecorators(dynamicMPPSDecorators, MPPSService.class.getName());
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<QueryService>> getConfiguredQueryServiceDynamicDecorators() {
		return queryDecorators.getOrderedDecorators(dynamicQueryDecorators, QueryService.class.getName());
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<RetrieveService>> getConfiguredRetrieveServiceDynamicDecorators() {
		return retrieveDecorators.getOrderedDecorators(dynamicRetrieveDecorators, RetrieveService.class.getName());
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<CStoreSCUService>> getConfiguredCStoreSCUServiceDynamicDecorators() {
		return cstoreSCUDecorators.getOrderedDecorators(dynamicCStoreSCUDecorators, CStoreSCUService.class.getName());
	}

	@Produces @RequestScoped
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<DerivedStudyFields>> getConfiguredDerivedStudyFieldsDynamicDecorators() {
		return derivedStudyDecorators.getOrderedDecorators(dynamicDerivedStudyFieldsDecorators,
				DerivedStudyFields.class.getName(), false); //false => not using cache
	}

	@Produces @RequestScoped
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<DerivedSeriesFields>> getConfiguredDerivedSeriesFieldsDynamicDecorators() {
		return derivedSeriesDecorators.getOrderedDecorators
				(dynamicDerivedSeriesFieldsDecorators,
				DerivedSeriesFields.class.getName(), false); //false => not using cache
	}

}
