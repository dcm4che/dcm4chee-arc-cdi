package org.dcm4chee.conf.decorators;

import org.dcm4chee.archive.mpps.MPPSService;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.conf.decorators.ConfiguredDynamicDecorators;
import org.dcm4chee.conf.decorators.DelegatingServiceImpl;
import org.dcm4chee.conf.decorators.DynamicDecorator;
import org.dcm4chee.conf.decorators.ServiceDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import java.util.Collection;

@ApplicationScoped
public class DynamicDecoratorProducer {
	private static final Logger LOG = LoggerFactory.getLogger(DynamicDecoratorProducer.class);
	
	@Inject 
	@DynamicDecorator
	Instance<DelegatingServiceImpl<StoreService>> dynamicStoreDecorators;
	
	@Inject
	@DynamicDecorator
	Instance<DelegatingServiceImpl<MPPSService>> dynamicMPPSDecorators;
	
	@Inject
	private ServiceDecorator<StoreService> storeDecorators;
	
	@Inject
	private ServiceDecorator<MPPSService> mppsDecorators;
	
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
	
}
