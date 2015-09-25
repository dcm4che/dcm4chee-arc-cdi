package org.dcm4chee.archive.processing.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.dto.ActiveService;
import org.dcm4chee.archive.entity.ActiveProcessing;
import org.dcm4chee.archive.processing.ActiveProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API to Add, Remove and Query ActiveProcesses. 
 *
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
@Stateless
public class ActiveProcessingServiceImpl implements ActiveProcessingService {

    private static final Logger LOG = LoggerFactory.getLogger(ActiveProcessingServiceImpl.class);

    @PersistenceContext(name = "dcm4chee-arc")
    private EntityManager em;

    @Override
    public boolean addActiveProcess(String studyIUID, String seriesIUID, String sopIUID, ActiveService service) {
        return addActiveProcess(sopIUID, sopIUID, sopIUID, service, null);
    }
    @Override
    public boolean addActiveProcess(String studyIUID, String seriesIUID, String sopIUID, ActiveService service, Attributes attrs) {
        ActiveProcessing activeProcess = new ActiveProcessing();
        activeProcess.setStudyInstanceUID(studyIUID);
        activeProcess.setSeriesInstanceUID(seriesIUID);
        activeProcess.setSopInstanceUID(sopIUID);
        activeProcess.setActiveService(service);
        activeProcess.setAttributes(attrs);
        boolean persisted = false;
        try{
            em.persist(activeProcess);
            persisted = true;
        }
        catch(Exception e) {
            LOG.error("Unable to persist Active Process with studyIUID = {}, "
                    + "seriesIUID = {} and sopIUID = {} for service {} - "
                    + "reason {}", studyIUID, seriesIUID, sopIUID, service, e);
        }
        return persisted;
    }

    @Override
    public boolean isStudyUnderProcessingByServices(String studyIUID, List<ActiveService> services) {
        Query query = em.createNamedQuery(ActiveProcessing.IS_STUDY_BEING_PROCESSED);
        query.setParameter("uid", studyIUID);
        query.setParameter("serviceList", services);
        try{
            return (Long) query.getSingleResult() > 0;
        }
        catch (Exception e)
        {
            LOG.error("Unable to check active processing status for the provided"
                    + " study {} - reason {}", studyIUID, e);
            return false;
        }
    }


    @Override
    public List<ActiveProcessing> getActiveProcessesByStudy(String studyIUID, ActiveService activeService) {
        return queryActiveProcesses(studyIUID, activeService, ActiveProcessing.FIND_BY_STUDY_IUID_AND_SERVICE, ActiveProcessing.FIND_BY_STUDY_IUID);
    }

    @Override
    public List<ActiveProcessing> getActiveProcessesBySeries(String seriesIUID, ActiveService activeService) {
        return queryActiveProcesses(seriesIUID, activeService, ActiveProcessing.FIND_BY_SERIES_IUID_AND_SERVICE, ActiveProcessing.FIND_BY_SERIES_IUID);
    }

    @Override
    public List<ActiveProcessing> getActiveProcessesBySOPInstanceUID(String sopIUID, ActiveService activeService) {
        return queryActiveProcesses(sopIUID, activeService, ActiveProcessing.FIND_BY_SOP_IUID_AND_SERVICE, ActiveProcessing.FIND_BY_SOP_IUID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActiveProcessing> getActiveProcessesBySOPInstanceUIDs(List<String> sopIUIDs, ActiveService activeService) {
        Query query = em.createNamedQuery(activeService == null ? ActiveProcessing.FIND_BY_SOP_IUIDs : ActiveProcessing.FIND_BY_SOP_IUIDs_AND_SERVICE);
        query.setParameter("uidList", sopIUIDs);
        if (activeService != null)
            query.setParameter("service", activeService);
        try{
            return query.getResultList();
        } catch (Exception e) {
            LOG.error("Unable to get active processes for {} and the provided SOPIUIDs! reason {}, return null", activeService, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<ActiveProcessing> queryActiveProcesses(String uid, ActiveService activeService, String queryNameWithService, String queryNameWithoutService) {
        String queryName = activeService == null ? queryNameWithoutService : queryNameWithService;
        Query query = em.createNamedQuery(queryName);
        query.setParameter("uid", uid);
        if (activeService != null)
            query.setParameter("service", activeService);
        try{
            return query.getResultList();
        } catch (Exception e) {
            LOG.error("Unable to get active processes for {}! service: {} uid: {}  reason {}, return null", queryName, activeService, uid, e);
            return null;
        }
    }

    @Override
    public boolean deleteActiveProcessBySOPInstanceUIDandService(String sopIUID, ActiveService service) {
        Query query = em.createNamedQuery(ActiveProcessing.DELETE_BY_SOP_IUID_AND_SERVICE);
        query.setParameter("uid", sopIUID);
        query.setParameter("service", service);
        int result = 0;
        try{
            result = query.executeUpdate();
        }
        catch(Exception e){
            LOG.error("Unable to delete provided Active Process with SOPIUID "
                    + "= {} for service {} - reason {}", sopIUID, service, e);
        }
        return result == 0 ? false : true;
    }

    @Override
    public boolean deleteActiveProcessBySOPInstanceUIDsAndService(List<String> sopIUIDs, ActiveService service) {
        Query query = em.createNamedQuery(ActiveProcessing.DELETE_BY_SOP_IUIDs_AND_SERVICE);
        query.setParameter("uidList", sopIUIDs);
        query.setParameter("service", service);
        int result = 0;
        try{
            result = query.executeUpdate();
        }
        catch(Exception e){
            LOG.error("Unable to delete provided Active Process with SOP IUIDs "
                    + "= {} for service {} - reason {}", sopIUIDs, service, e);
        }
        return result == 0 ? false : true;
    }

}
