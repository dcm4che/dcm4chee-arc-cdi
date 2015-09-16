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
	private DynamicDecoratorManager decoratorManager;
	

	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<StoreService>> getConfiguredStoreServiceDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicStoreDecorators, StoreService.class);
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<MPPSService>> getConfiguredMPPSServiceDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicMPPSDecorators, MPPSService.class);
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<QueryService>> getConfiguredQueryServiceDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicQueryDecorators, QueryService.class);
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<RetrieveService>> getConfiguredRetrieveServiceDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicRetrieveDecorators, RetrieveService.class);
	}
	
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<CStoreSCUService>> getConfiguredCStoreSCUServiceDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicCStoreSCUDecorators, CStoreSCUService.class);
	}

	@Produces @RequestScoped
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<DerivedStudyFields>> getConfiguredDerivedStudyFieldsDynamicDecorators() {
		return decoratorManager.getOrderedDecorators(dynamicDerivedStudyFieldsDecorators,
				DerivedStudyFields.class, false); //false => not using cache
	}

	@Produces @RequestScoped
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<DerivedSeriesFields>> getConfiguredDerivedSeriesFieldsDynamicDecorators() {
		return decoratorManager.getOrderedDecorators
				(dynamicDerivedSeriesFieldsDecorators,
				DerivedSeriesFields.class, false); //false => not using cache
	}

}
