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
package org.dcm4chee.archive.conf;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;

/**
 * @author Steve Kroetsch<stevekroetsch@hotmail.com>
 *
 */
@LDAP(objectClasses = "dcmUnarchivedInstanceDetector")
@ConfigurableClass
public class UnarchivedInstanceDetector implements Serializable {

    private static final long serialVersionUID = 3454101146694795927L;

    public static final String MARKER_DATE_FORMAT = "yyyy-MM-dd";

    @ConfigurableProperty(name = "dcmUnarchivedInstanceDetectorScanInterval", defaultValue = "0")
    private int scanInterval = 0;

    @ConfigurableProperty(name = "dcmUnarchivedInstanceDetectorBatchSize", defaultValue = "1000")
    private int batchSize = 1000;

    @ConfigurableProperty(name = "dcmUnarchivedInstanceDetectorScanMarker")
    private String scanMarker;

    @ConfigurableProperty(name = "dcmUnarchivedInstanceDetectorScanDuration", defaultValue = "4w")
    private String scanDuration = "4w";

    @ConfigurableProperty(name = "dcmUnarchivedInstanceDetectorNonScanablePeriod", defaultValue = "4w")
    private String nonScanablePeriod = "4w";

    private Timestamp scanMarkerTimestamp;
    private long scanDurationInMs = 2419200000L; // 4 weeks
    private long nonScanablePeriodInMs = 2419200000L; // 4 weeks

    public void setScanInterval(int scanInterval) {
        this.scanInterval = scanInterval;
    }

    public int getScanInterval() {
        return scanInterval;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setScanMarker(String scanMarker) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(MARKER_DATE_FORMAT);
        scanMarkerTimestamp = new Timestamp(dateFormat.parse(scanMarker).getTime());
        this.scanMarker = scanMarker;
    }

    public String getScanMarker() {
        return scanMarker;
    }

    public Timestamp getScanMarkerTimestamp() {
        return scanMarkerTimestamp;
    }

    public void setScanDuration(String scanDuration) {
        this.scanDurationInMs = Utils.parseTimeInterval(scanDuration);
        this.scanDuration = scanDuration;
    }

    public String getScanDuration() {
        return scanDuration;
    }

    public long getScanDurationInMs() {
        return scanDurationInMs;
    }

    public void setNonScanablePeriod(String nonScanablePeriod) {
        nonScanablePeriodInMs = Utils.parseTimeInterval(nonScanablePeriod);
        this.nonScanablePeriod = nonScanablePeriod;
    }

    public String getNonScanablePeriod() {
        return nonScanablePeriod;
    }

    public long getNonScanablePeriodInMs() {
        return nonScanablePeriodInMs;
    }

}
