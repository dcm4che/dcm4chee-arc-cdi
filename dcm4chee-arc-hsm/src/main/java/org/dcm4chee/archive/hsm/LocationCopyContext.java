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
package org.dcm4chee.archive.hsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.dcm4chee.archive.entity.Location;

/**
 * @author Steve Kroetsch <stevekroetsch@hotmail.com>
 *
 */
public class LocationCopyContext implements Serializable {

    private static final long serialVersionUID = -4798090626720340878L;

    private String sourceStorageSystemGroupID;
    private String targetStorageSystemGroupID;
    private boolean deleteSourceLocation = false;
    private HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
    private ArrayList<Location> copies = new ArrayList<Location>();
    private String jmsMessageID;

    public void setSourceStorageSystemGroupID(String sourceStorageSystemGroupID) {
        this.sourceStorageSystemGroupID = sourceStorageSystemGroupID;
    }

    public String getSourceStorageSystemGroupID() {
        return sourceStorageSystemGroupID;
    }

    public void setTargetStorageSystemGroupID(String targetStorageSystemGroupID) {
        this.targetStorageSystemGroupID = targetStorageSystemGroupID;
    }

    public String getTargetStorageSystemGroupID() {
        return targetStorageSystemGroupID;
    }

    public void setDeleteSourceLocation(boolean deleteSourceLocation) {
        this.deleteSourceLocation = deleteSourceLocation;
    }

    public boolean getDeleteSourceLocaton() {
        return deleteSourceLocation;
    }

    public void addCopy(Location copy) {
        copies.add(copy);
    }

    public Collection<Location> getCopies() {
        return Collections.unmodifiableCollection(copies);
    }

    public void setJMSMessageID(String jmsMessageID) {
        this.jmsMessageID = jmsMessageID;
    }

    public String getJMSMessageID() {
        return jmsMessageID;
    }

    public Serializable getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Serializable value) {
        properties.put(key, value);
    }
}
