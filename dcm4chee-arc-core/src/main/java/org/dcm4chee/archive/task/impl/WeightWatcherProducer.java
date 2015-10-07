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
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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

package org.dcm4chee.archive.task.impl;

import org.dcm4che3.conf.core.api.ConfigChangeEvent;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.WeightWatcherConfiguration;
import org.dcm4chee.task.WeightWatcher;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Produces the singleton {@WeightWatcher} instance.
 *
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
@ApplicationScoped
public class WeightWatcherProducer {

    @Inject
    private Device device;

    private WeightWatcherImpl weightWatcher;

    @PostConstruct
    private void init() {
        weightWatcher = new WeightWatcherImpl(getWeightWatcherConfiguration(device), Runtime.getRuntime().maxMemory());
    }

    @Produces
    public WeightWatcher produce() {
        return weightWatcher;
    }

    public void onConfigChange(@Observes ConfigChangeEvent configChange) {
        weightWatcher.reconfigure(getWeightWatcherConfiguration(device));
    }

    private static WeightWatcherConfiguration getWeightWatcherConfiguration(Device device) {
        ArchiveDeviceExtension archiveDevExtension = device.getDeviceExtension(ArchiveDeviceExtension.class);
        WeightWatcherConfiguration weightWatcherConfig = archiveDevExtension.getWeightWatcherConfiguration();

        if (weightWatcherConfig == null)
            weightWatcherConfig = new WeightWatcherConfiguration(); // default config

        return weightWatcherConfig;
    }
}
