package org.dcm4chee.archive.processing.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    @PersistenceContext(name = "dcm4chee-arc", unitName="dcm4chee-arc")
    private EntityManager em;

    @Override
    public boolean addActiveProcess(String studyIUID, String seriesIUID, String sopIUID, ActiveService service) {
        ActiveProcessing activeProcess = new ActiveProcessing();
        activeProcess.setStudyInstanceUID(studyIUID);
        activeProcess.setSeriesInstanceUID(seriesIUID);
        activeProcess.setSopInstanceUID(sopIUID);
        activeProcess.setActiveService(service);
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

    @SuppressWarnings("unchecked")
    @Override
    public boolean isStudyUnderProcessingByServices(String studyIUID, List<ActiveService> services) {
        Query query = em.createNamedQuery(ActiveProcessing.IS_STUDY_BEING_PROCESSED);
        query.setParameter(1, studyIUID);
        query.setParameter("serviceList", services);
        Long result = 0l ;
        try{
        result = (Long) query.getSingleResult();
        }
        catch (Exception e)
        {
            LOG.error("Unable to check active processing status for the provided"
                    + " study {} - reason {}", studyIUID, e);
        }
        return result == 0l ? false : true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActiveProcessing> getActiveProcessesByStudy(String studyIUID) {
        Query query = em.createNamedQuery(ActiveProcessing.FIND_BY_STUDY_IUID);
        query.setParameter(1, studyIUID);
        List<ActiveProcessing> result = new ArrayList<ActiveProcessing>();
        try{
        result = query.getResultList();
        }
        catch (Exception e)
        {
            LOG.error("Unable to get active processes for the provided study "
                    + "{} - reason {}, empty result returned", studyIUID, e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActiveProcessing> getActiveProcessesBySeries(String seriesIUID) {
        Query query = em.createNamedQuery(ActiveProcessing.FIND_BY_SERIES_IUID);
        query.setParameter(1, seriesIUID);
        List<ActiveProcessing> result = new ArrayList<ActiveProcessing>();
        try{
        result = query.getResultList();
        }
        catch (Exception e)
        {
            LOG.error("Unable to get active processes for the provided series "
                    + "{} - reason {}, empty result returned", seriesIUID, e);
        }
        return result;
    }

    @Override
    public ActiveProcessing getActiveProcessesBySOPInstanceUID(String sopIUID) {
        Query query = em.createNamedQuery(ActiveProcessing.FIND_BY_SOP_IUID);
        query.setParameter(1, sopIUID);
        ActiveProcessing result = null;
        try{ 
        result = (ActiveProcessing) query.getSingleResult();
        }
        catch (Exception e)
        {
            LOG.error("Unable to get active process for the provided sopIUID"
                    + " {} - reason {}, null value returned", sopIUID, e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ActiveProcessing> getActiveProcessesBySOPInstanceUIDs(List<String> sopIUIDs) {
        Query query = em.createNamedQuery(ActiveProcessing.FIND_BY_SOP_IUIDs);
        query.setParameter("uidList", sopIUIDs);
        List<ActiveProcessing> result = new ArrayList<ActiveProcessing>();
        try{
        result = query.getResultList();
        }
        catch (Exception e)
        {
            LOG.error("Unable to get active processes for the provided SOPIUIDs "
                    + "- reason {}, empty result returned", e);
        }
        return result;
    }

    @Override
    public boolean deleteActiveProcessBySOPInstanceUIDandService(String sopIUID, ActiveService service) {
        Query query = em.createNamedQuery(ActiveProcessing.DELETE_BY_SOP_IUID_AND_PROCESS);
        query.setParameter(1, sopIUID);
        query.setParameter(2, service);
        int result = 0;
        try{
        result = query.executeUpdate();
        }
        catch(Exception e){
            LOG.error("Unable to delete provided Active Process with SOPIUID "
                    + "= {} for service {} - reason {}", sopIUID, service, e);
        }
        return result == 0? false : true;
    }

}
