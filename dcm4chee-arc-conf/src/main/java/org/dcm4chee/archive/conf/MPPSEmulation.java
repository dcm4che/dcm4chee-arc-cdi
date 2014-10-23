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

import java.util.HashMap;
import java.util.Map;

import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
@ConfigClass(objectClass = "dcmMPPSEmulation")
public class MPPSEmulation {

    @ConfigField(name = "dcmMPPSEmulationPollInterval")
    private int pollInterval;

    // will be converted to Collection<Rule>, when supported by generic configuration
    @ConfigField(name = "dcmMPPSEmulationRules", mapKey = "cn", failIfNotPresent=false)
    private Map<String, Rule> rules = new HashMap<String, Rule>();

    private Map<String, Rule> ruleMap = new HashMap<String, Rule>();

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Map<String, Rule> getRules() {
        return rules;
    }

    public void setRules(Map<String, Rule> rules) {
        this.rules.clear();
        this.ruleMap.clear();
        for (Rule rule : rules.values()) {
            addMPPSEmulationRule(rule);
        }
    }

    public Rule getMPPSEmulationRule(String sourceAET) {
        return ruleMap.get(sourceAET);
    }

    public void addMPPSEmulationRule(Rule rule) {
        rules.put(rule.getCommonName(), rule);
        for (String sourceAET : rule.getSourceAETs()) {
            ruleMap.put(sourceAET, rule);
        }
    }

    @ConfigClass(objectClass = "dcmMPPSEmulationRule")
    public static final class Rule {

        @ConfigField(name = "cn")
        private String commonName;

        @ConfigField(name = "dcmAETitle")
        private String[] sourceAETs;

        @ConfigField(name = "dcmMPPSEmulationDelay")
        private int emulationDelay;

        public String getCommonName() {
            return commonName;
        }

        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }

        public String[] getSourceAETs() {
            return sourceAETs;
        }

        public void setSourceAETs(String... sourceAETs) {
            this.sourceAETs = sourceAETs;
        }

        public int getEmulationDelay() {
            return emulationDelay;
        }

        public void setEmulationDelay(int emulationDelay) {
            this.emulationDelay = emulationDelay;
        }
    }

}
