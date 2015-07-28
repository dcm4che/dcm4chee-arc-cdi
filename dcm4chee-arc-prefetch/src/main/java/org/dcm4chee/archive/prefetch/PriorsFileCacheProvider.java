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
package org.dcm4chee.archive.prefetch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.prefetch.impl.PriorsCacheProviderEJB;
import org.dcm4chee.storage.RetrieveContext;
import org.dcm4chee.storage.StorageContext;
import org.dcm4chee.storage.conf.Availability;
import org.dcm4chee.storage.conf.FileCache;
import org.dcm4chee.storage.conf.StorageSystem;
import org.dcm4chee.storage.conf.StorageSystemGroup;
import org.dcm4chee.storage.service.StorageService;
import org.dcm4chee.storage.spi.FileCacheProvider;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@Named("org.dcm4chee.archive.prefetch.priorscache")
@Dependent
public class PriorsFileCacheProvider implements FileCacheProvider {

    @Inject
    private Device device;

    @Inject
    private PriorsCacheProviderEJB ejb;

    @Inject
    private StorageService storageService;

    private Map<Path, StorageSystem> resolvedPaths;

    private String groupID;

    private FileCache fileCache;

    @Override
    public void init(FileCache fileCache) {
        this.fileCache = fileCache;
        groupID = storageSystemGroup(fileCache);
        ArchiveDeviceExtension devExt = device
                .getDeviceExtension(ArchiveDeviceExtension.class);
        final int maxEntries = devExt.getPriorsCacheMaxResolvedPathEntries();
        resolvedPaths = Collections
                .synchronizedMap(new LinkedHashMap<Path, StorageSystem>() {
                    @Override
                    protected boolean removeEldestEntry(
                            Map.Entry<Path, StorageSystem> eldest) {
                        return size() > maxEntries;
                    }
                });
    }

    private String storageSystemGroup(FileCache fileCache) {
        String groupID = fileCache.getStorageSystemGroupID();
        if (groupID != null)
            return groupID;
        String groupType = fileCache.getStorageSystemGroupType();
        if (groupType == null)
            throw new IllegalStateException(
                    "Storage System Group ID or Storage System Type not defined");
        StorageSystemGroup group = storageService.selectBestStorageSystemGroup(groupType);
        if (group == null)
            throw new IllegalStateException(
                    "No Storage System Group defined for type " + groupType);
        return group.getGroupID();
    }

    @Override
    public Path toPath(RetrieveContext ctx, String name) throws IOException {
        Path path = Paths.get(ctx.getStorageSystem().getStorageSystemID(), name);
        StorageSystem system = resolveStorageSystem(path);
        ctx.setProperty(StorageSystem.class.getName(), system);
        return storageService.getBaseDirectory(system).resolve(path);
    }

    @Override
    public Path toPath(StorageContext ctx, String name) throws IOException {
        throw new UnsupportedOperationException();
    }

    private StorageSystem resolveStorageSystem(Path path) throws IOException {
        StorageSystem system = resolvedPaths.get(path);
        if (system == null)
            system = resolvedPaths.get(path.getParent());
        if (system == null) {
            system = selectStorageSystem();
            StorageSystem prev;
            synchronized (resolvedPaths) {
                prev = resolvedPaths.get(path);
                if (prev == null) {
                    resolvedPaths.put(path, system);
                    resolvedPaths.put(path.getParent(), system);
                }
            }
            if (prev != null)
                system = prev;
        }
        return system;
    }

    private StorageSystem selectStorageSystem() throws IOException {
        StorageSystem storageSystem = storageService.selectStorageSystem(groupID, 0);
        if (storageSystem == null)
            throw new IOException("No writeable Storage System in Storage System Group "
                    + groupID);
        return storageSystem;
    }

    @Override
    public boolean access(Path path) throws IOException {
        return Files.exists(path);
    }

    @Override
    public void register(final RetrieveContext ctx, final String name, Path path)
            throws IOException {
        final StorageSystem system = (StorageSystem) ctx.getProperty(StorageSystem.class
                .getName());
        final String storagePath = storageService.getBaseDirectory(system)
                .relativize(path).toString();
        final Availability availability = system.getAvailability();
        final String iuid;
        try (DicomInputStream in = new DicomInputStream(path.toFile())) {
            iuid = in.readFileMetaInformation().getString(Tag.MediaStorageSOPInstanceUID);
        }

        device.execute(new Runnable() {
            @Override
            public void run() {
                ejb.register(ctx.getStorageSystem(), name, system, storagePath, iuid,
                        availability);
            }
        });
    }

    @Override
    public void register(StorageContext ctx, String name, Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearCache() throws IOException {
        resolvedPaths.clear();
        ejb.clearCache(groupID);
    }
}
