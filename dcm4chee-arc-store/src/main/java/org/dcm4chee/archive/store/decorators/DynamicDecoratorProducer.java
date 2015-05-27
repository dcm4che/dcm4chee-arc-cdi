package org.dcm4chee.archive.store.decorators;

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
	
//	@Inject
//	@DynamicDecorator
//	Instance<DelegatingServiceImpl<QueryService>> dynamicQueryDecorators;
	
	@Inject
	private ServiceDecorator<StoreService> storeDecorators;
	
//	@Inject
//	private ServiceDecorator<QueryService> queryDecorators;
	
	//TODO: change synchronization from method level
	// each object can only have one synchronized method invoked at a time, so getConfiguredStoreServiceDynamicDecorators will block getConfiguredQueryServiceDynamicDecorator
	@Produces
	@ConfiguredDynamicDecorators
	public Collection<DelegatingServiceImpl<StoreService>> getConfiguredStoreServiceDynamicDecorators() {
		return storeDecorators.getOrderedDecorators(dynamicStoreDecorators, StoreService.class.getName());
	}
	
//	@Produces
//	@ConfiguredDynamicDecorators
//	public synchronized Collection<DelegatingServiceImpl<QueryService>> getConfiguredQueryServiceDynamicDecorators() {
//		return queryDecorators.getOrderedDecorators(dynamicQueryDecorators, QueryService.class.getName());
//	}
	
}
