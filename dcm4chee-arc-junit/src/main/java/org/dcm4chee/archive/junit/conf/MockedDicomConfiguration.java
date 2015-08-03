package org.dcm4chee.archive.junit.conf;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;

import java.util.HashMap;
import java.util.Map;

public class MockedDicomConfiguration implements DicomConfiguration {

    Map<String, Device> deviceMap = new HashMap<>();

    @Override
    public ApplicationEntity findApplicationEntity(String aet) throws ConfigurationException {
        for (Map.Entry<String, Device> stringDeviceEntry : deviceMap.entrySet()) {
            ApplicationEntity applicationEntity = stringDeviceEntry.getValue().getApplicationEntity(aet);
            if (applicationEntity!=null) return applicationEntity;
        }

        return null;
    }

    @Override
    public ApplicationEntity findApplicationEntityByUUID(String uuid) throws ConfigurationException {
        return null;
    }

    @Override
    public Device findDeviceByUUID(String uuid) throws ConfigurationException {
        return null;
    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        return deviceMap.get(name);
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        if (deviceMap.containsKey(device.getDeviceName())) throw new ConfigurationException();
        deviceMap.put(device.getDeviceName(), device);
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
        deviceMap.put(device.getDeviceName(), device);
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        deviceMap.remove(name);
    }

    @Override
    public String[] listDeviceNames() throws ConfigurationException {
        return new String[0];
    }

    @Override
    public void sync() throws ConfigurationException {

    }

    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        return null;
    }

    @Override
    public void runBatch(DicomConfigBatch dicomConfigBatch) {
        dicomConfigBatch.run();
    }
}
