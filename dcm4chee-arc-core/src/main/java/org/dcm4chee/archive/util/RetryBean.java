package org.dcm4chee.archive.util;

import org.dcm4che3.net.Device;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.concurrent.Callable;

/**
 * Created by umberto on 9/30/15.
 */
@Dependent
public class RetryBean<T, E extends Exception> {

    @Inject Device device;

    public T retry (Callable<T> callable) throws E {
        ArchiveDeviceExtension dE = device.getDeviceExtension(ArchiveDeviceExtension.class);
        return new RetryTask<T,E>(dE.getUpdateDbRetries(), dE.getUpdateDbDelay(), callable).call();
    }
}
