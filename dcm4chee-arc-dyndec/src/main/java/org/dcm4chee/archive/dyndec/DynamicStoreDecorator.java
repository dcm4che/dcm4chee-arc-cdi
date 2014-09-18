package org.dcm4chee.archive.dyndec;

import java.util.Map;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveAEExtension.Composition;
import org.dcm4chee.archive.dyndec.pipelines.BasicStorePipeLine;
import org.dcm4chee.archive.store.StoreContext;
import org.dcm4chee.archive.store.StoreService;
import org.dcm4chee.archive.store.StoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Decorator
public abstract class DynamicStoreDecorator implements StoreService {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicStoreDecorator.class);
    @Inject
    @Delegate
    StoreService storeService;

    @Override
    public void coerceAttributes(StoreContext context) throws DicomServiceException {
        StoreSession session = context.getStoreSession();
        ArchiveAEExtension arcAE = session.getArchiveAEExtension();
        Composition bpeEmulator = arcAE.getComposition();
        Map<String, String> pipelines = bpeEmulator.getCompositionMap();
        String pipelineImplementation = pipelines.get(session.getRemoteAET());
        ClassLoader cl = this.getClass().getClassLoader();
        BasicStorePipeLine pipeLine = null;
        if (pipelineImplementation != null) {

            LOG.debug("Found PipeLine for the calling AETitle {}",session.getRemoteAET());

            storeService.coerceAttributes(context);
            if (!pipelineImplementation.contains("java:")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<BasicStorePipeLine> result = (Class<BasicStorePipeLine>) cl
                            .loadClass(pipelineImplementation);
                    pipeLine = result.newInstance();
                } catch (ClassNotFoundException | InstantiationException
                        | IllegalAccessException e) {
                    LOG.error("Unable to Load PipeLine implementation class {}", pipelineImplementation);
                }

            } else {
                try {
                    Context ctx = new InitialContext();
                    pipeLine = (BasicStorePipeLine) ctx.lookup(pipelineImplementation);
                } catch (NamingException e) {
                    LOG.error("Incorrect JNDI path defined for pipe line implementation bean defined with "
                            + "path {}", pipelineImplementation);
                }
            }
            LOG.debug("Applying pipeline on receiving store request from {} with custom implementation for "
                    + "coercion {}", session.getRemoteAET(), pipelineImplementation);
            pipeLine.coerceAttributes(context);
        }

    }
}
