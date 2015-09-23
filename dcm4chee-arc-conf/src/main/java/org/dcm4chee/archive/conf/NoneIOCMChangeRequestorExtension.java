/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4chee.archive.conf;

import java.util.ArrayList;
import java.util.Collection;

import org.dcm4che3.conf.api.extensions.ReconfiguringIterator;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;

/**
 *  
 * @author Franz Willer <franz.willer@gmail.com>
 *
 */
@LDAP(objectClasses = "dcmNoneIOCMConfigClass")
@ConfigurableClass
public class NoneIOCMChangeRequestorExtension extends DeviceExtension {

    private static final long serialVersionUID = 5132577431532807887L;
    
    @ConfigurableProperty(name = "dcmNoneIOCMChangeRequestorDevices",
    		description="List of Devices which are allowed to perform None IOCM change requests. (allowing structural changes)",
    		collectionOfReferences=true)
    private Collection<Device> noneIOCMChangeRequestorDevices = new ArrayList<Device>();
    
    @ConfigurableProperty(name = "dcmNoneIOCMModalityDevices",
    		description="List of Devices which are allowed to perform None IOCM Instance update. (no structural changes)",
    		collectionOfReferences=true)
    private Collection<Device> noneIOCMModalityDevices = new ArrayList<Device>();

    @ConfigurableProperty(name = "dcmGracePeriod", description="Grace Period in seconds within Instance updates are allowed. Period starts with QC Deletion of instance", defaultValue="0")
    private int gracePeriod = 0;

	public Collection<Device> getNoneIOCMChangeRequestorDevices() {
		return noneIOCMChangeRequestorDevices;
	}

	public void setNoneIOCMChangeRequestorDevices(Collection<Device> noneIOCMChangeRequestorDevices) {
		this.noneIOCMChangeRequestorDevices = noneIOCMChangeRequestorDevices;
	}
	
	public void addNoneIOCMChangeRequestorDevices(Device d) {
		this.noneIOCMChangeRequestorDevices.add(d);
	}
	public boolean removeNoneIOCMChangeRequestorDevices(Device d) {
		return this.noneIOCMChangeRequestorDevices.remove(d);
	}

	public Collection<Device> getNoneIOCMModalityDevices() {
		return noneIOCMModalityDevices;
	}

	public void setNoneIOCMModalityDevices(Collection<Device> noneIOCMModalityDevices) {
		this.noneIOCMModalityDevices = noneIOCMModalityDevices;
	}
	
	public void addNoneIOCMModalityDevices(Device d) {
		this.noneIOCMModalityDevices.add(d);
	}
	public boolean removeNoneIOCMModalityDevices(Device d) {
		return this.noneIOCMModalityDevices.remove(d);
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

    @Override
    public void reconfigure(DeviceExtension from) {
    	NoneIOCMChangeRequestorExtension noneIOCM = (NoneIOCMChangeRequestorExtension) from;
        ReconfiguringIterator.reconfigure(noneIOCM, this, NoneIOCMChangeRequestorExtension.class);
    }

}