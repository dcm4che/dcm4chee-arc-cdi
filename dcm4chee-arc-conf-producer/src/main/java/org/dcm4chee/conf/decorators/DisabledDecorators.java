package org.dcm4chee.conf.decorators;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisabledDecorators {
	@Inject
	Device device;
	
	private static final Logger LOG = LoggerFactory.getLogger(DisabledDecorators.class);
	
	private List<String> disabledDecorators = null;
	
	//TODO: how to detect changes to this property, and re-generate the service decorators after?
	@Produces
	@ConfiguredDynamicDecorators
	private synchronized List<String> produceDisabledDecorators() {
		if (disabledDecorators == null) {
			ArchiveDeviceExtension devExt = device.getDeviceExtension(ArchiveDeviceExtension.class);
			if (devExt != null) {
				disabledDecorators = Arrays.asList(devExt.getDisabledDecorators());
			}
		}
		LOG.debug("Returning disabled decorators: {}", disabledDecorators);
		return disabledDecorators;
	}
	
}
