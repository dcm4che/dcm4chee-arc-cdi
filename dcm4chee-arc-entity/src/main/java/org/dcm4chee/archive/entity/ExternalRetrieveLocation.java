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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4chee.archive.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.dcm4chee.storage.conf.Availability;


/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */
@NamedQueries({
    @NamedQuery(
            name=ExternalRetrieveLocation.FIND_EXT_LOCATIONS_BY_IUID_DEVICE_NAME,
            query = "Select e from ExternalRetrieveLocation e "
                    + " where e.instance.sopInstanceUID = ?1 and e.retrieveDeviceName = ?2"),
    @NamedQuery(
            name=ExternalRetrieveLocation.FIND_EXT_LOCATIONS_BY_IUID,
            query = "Select e from ExternalRetrieveLocation e "
                    + " where e.instance.sopInstanceUID = ?1"),
    @NamedQuery(
            name=ExternalRetrieveLocation.FIND_EXT_LOCATIONS_BY_IUID_AVAILABILITY,
            query = "Select e from ExternalRetrieveLocation e"
                    + " where e.instance.sopInstanceUID = ?1 and e.availability = ?2")
})
@Entity
@Table(name="ext_retrieve_location")
public class ExternalRetrieveLocation implements Serializable {

    private static final long serialVersionUID = -8051311963967965531L;
    public static final String FIND_EXT_LOCATIONS_BY_IUID_DEVICE_NAME
     = "ExternalRetrieveLocation.findExtLocationsByIUIDDeviceName";
    public static final String FIND_EXT_LOCATIONS_BY_IUID
    = "ExternalRetrieveLocation.findExtLocationsByIUID";
    public static final String FIND_EXT_LOCATIONS_BY_IUID_AVAILABILITY
     = "ExternalRetrieveLocation.findExtLocationsByIUIDAvailability";
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "pk")
    private long pk;

    public ExternalRetrieveLocation() {
    }

    public ExternalRetrieveLocation(String retrieveDeviceName
            , Availability availability) {
        super();
        this.retrieveDeviceName = retrieveDeviceName;
        this.availability = availability;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "instance_fk")
    private Instance instance;

    @Column(name="retrieve_device_name")
    private String retrieveDeviceName;

    @Basic(optional = false)
    @Column(name = "availability")
    private Availability availability;

    public long getPk() {
        return pk;
    }

    public Instance getInstance() {
        return instance;
    }

    public String getRetrieveDeviceName() {
        return retrieveDeviceName;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public void setRetrieveDeviceName(String retrieveDeviceName) {
        this.retrieveDeviceName = retrieveDeviceName;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }
    
}
