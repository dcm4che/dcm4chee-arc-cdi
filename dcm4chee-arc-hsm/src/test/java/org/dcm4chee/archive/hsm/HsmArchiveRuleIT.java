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

package org.dcm4chee.archive.hsm;

import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4chee.archive.conf.ArchivingRule;
import org.dcm4chee.archive.conf.ArchivingRules;
import org.junit.Test;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * 
 */

public class HsmArchiveRuleIT {

    private static final String[] AET_VALUES = {"SRC_AET_1","SRC_AET_2","SRC_AET_3"};
    private static final String[] DEVICE_VALUES = {"DEVICE_1","DEVICE_2","DEVICE_3"};
    private static final String[] INSTITUTION_VALUES = {"INST_NAME_1","INST_NAME_2","INST_NAME_3"};
    private static final String[] DEPARTMENT_VALUES = {"DEP_NAME_1","DEP_NAME_2","DEP_NAME_3"};
    private static final String[] MODALITY_VALUES = {"MODALITY_1","MODALITY_2", "MODALITY_3"};
    private static final String GROUP_1 = "GROUP_1";
    private static final String GROUP_2 = "GROUP_2";
    private static final String GROUP_3 = "GROUP_3";
    private static final String DUMMY = "DUMMY";
    private static final String NOMATCH = "NOMATCH";
    
    private static final int DEVICE = 0;
    private static final int AET = 1;
    private static final int INSTITUTION = 2;
    private static final int DEPARTMENT = 3;
    private static final int MODALITY = 4;
    
    private static final String[][] VALUES = new String[][] {
        DEVICE_VALUES, AET_VALUES, INSTITUTION_VALUES, DEPARTMENT_VALUES, MODALITY_VALUES
    };
    
    private static final Method[] METHODS = new Method[]{
        toMethod("setDeviceNames"),
        toMethod("setAeTitles"),
        toMethod("setInstitutionNames"),
        toMethod("setInstitutionalDepartmentNames"),
        toMethod("setModalities")
    };
    
    private static final Attributes DUMMY_ATTRS = getAttributes(DUMMY,DUMMY,DUMMY);
 
    @Test
    public void testMatchAET() throws Exception {
        checkMatching(AET);
    }

    @Test
    public void testMatchDevice() throws Exception {
        checkMatching(DEVICE);
    }
    
    @Test
    public void testMatchModality() throws Exception {
        checkMatching(MODALITY);
    }
    @Test
    public void testMatchInstitutionName() throws Exception {
        checkMatching(INSTITUTION);
    }
    @Test
    public void testMatchDepartmentName() throws Exception {
        checkMatching(DEPARTMENT);
    }

    @Test
    public void testMatchTwoRulesSameAET() throws Exception {
        ArchivingRules rules = new ArchivingRules();
        ArchivingRule rule1 = getRule(AET_VALUES[0], 60, GROUP_1);
        rule1.setAeTitles(new String[]{AET_VALUES[0]});
        ArchivingRule rule2 = getRule(AET_VALUES[0]+".1", 60, GROUP_2);
        rule2.setAeTitles(new String[]{AET_VALUES[0]});
        ArchivingRule rule3 = getRule(AET_VALUES[1], 60, GROUP_3);
        rule3.setAeTitles(new String[]{AET_VALUES[1]});
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        List<ArchivingRule> matching = rules.findArchivingRule(DUMMY, AET_VALUES[0], DUMMY_ATTRS);
        checkMatchingRules(AET_VALUES[0], matching, rules, rule1, rule2);
        matching = rules.findArchivingRule(DUMMY, AET_VALUES[1], DUMMY_ATTRS);
        checkMatchingRules(AET_VALUES[1], matching, rules, rule3);
    }

    @Test
    public void testMatchWithoutQueryValues() throws Exception {
        ArchivingRules rules = new ArchivingRules();
        ArchivingRule rule1 = getRule("DUMMY_AET", 60, GROUP_1);
        rule1.setAeTitles(new String[]{DUMMY});
        ArchivingRule rule2 = getRule("DUMMY_DEVICE", 60, GROUP_2);
        rule2.setDeviceNames(new String[]{DUMMY});
        ArchivingRule rule3 = getRule("DUMMY_INSTITUTION_AND_DEPARTMENT", 60, GROUP_3);
        rule3.setInstitutionNames(new String[]{DUMMY});
        rule3.setInstitutionalDepartmentNames(new String[]{DUMMY});
        ArchivingRule rule4 = getRule("DUMMY_MODALITY", 60, GROUP_3);
        rule4.setModalities(new String[]{DUMMY});
        rules.add(rule1);
        rules.add(rule2);
        rules.add(rule3);
        rules.add(rule4);
        List<ArchivingRule> matching = rules.findArchivingRule(null,null, new Attributes());
        checkMatchingRules("NO_QUERY_VALUES", matching, rules, rule1, rule2, rule3, rule4);
        matching = rules.findArchivingRule(DUMMY, DUMMY, getAttributes(NOMATCH, NOMATCH, NOMATCH));
        checkMatchingRules("DUMMY AET and DeviceName", matching, rules, rule1, rule2);
        matching = rules.findArchivingRule(DUMMY, DUMMY, getAttributes(NOMATCH, NOMATCH, DUMMY));
        checkMatchingRules("DUMMY AET, DeviceName and Modality", matching, rules, rule1, rule2, rule4);
        matching = rules.findArchivingRule(NOMATCH, NOMATCH, getAttributes(DUMMY, NOMATCH, NOMATCH));
        checkMatchingRules("DUMMY InstitutionName", matching, rules);
        matching = rules.findArchivingRule(NOMATCH, NOMATCH, getAttributes(NOMATCH, DUMMY, NOMATCH));
        checkMatchingRules("DUMMY DepartmentName", matching, rules);
        matching = rules.findArchivingRule(NOMATCH, NOMATCH, getAttributes(DUMMY, DUMMY, NOMATCH));
        checkMatchingRules("DUMMY InstitutionName and DepartmentName", matching, rules, rule3);
    }


    private void checkMatching(int idx) throws Exception {
        ArchivingRules rules = new ArchivingRules();
        String[] values = VALUES[idx];
        String[] queryValues = new String[] {DUMMY,DUMMY,DUMMY,DUMMY,DUMMY};
        ArchivingRule[][] expected = prepareArchivingRules(rules, METHODS[idx], values);
        for (int i = 0 ; i < values.length ; i++) {
            queryValues[idx] = values[i];
            List<ArchivingRule> matching = rules.findArchivingRule(queryValues[DEVICE], queryValues[AET], 
                    idx < INSTITUTION ? DUMMY_ATTRS : getAttributes(queryValues[INSTITUTION], queryValues[DEPARTMENT], queryValues[MODALITY]));
            checkMatchingRules(values[i], matching, rules, expected[i]);
        }
    }

    private ArchivingRule[][] prepareArchivingRules(ArchivingRules rules, Method method, String[] values) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        int len = 1 << values.length;
        ArchivingRule[][] expected = new ArchivingRule[values.length][(len >> 1)+1];
        int[] expectedIdx = new int[values.length];
        for (int i = 0 ; i < len ; i++) {
            ArchivingRule rule = getRule(method.getName().substring(3)+"_"+i, 60, GROUP_1);
            if (i == 0) {
                for (int k = 0 ; k < values.length ; k++) {
                    expected[k][expectedIdx[k]++]=rule;
                }
            } else {
                int j = 0;
                String[] ruleValues = new String[Integer.bitCount(i)];
                int mask = 0x01;
                for (int k = 0 ; k < values.length ; k++) {
                    if ( (i & mask) > 0) {
                        ruleValues[j++] = values[k];
                        expected[k][expectedIdx[k]++]=rule;
                    }
                    mask = mask << 1;
                }
                method.invoke(rule, new Object[]{ruleValues});
            }
            rules.add(rule);
        }
        return expected;
    }

    private ArchivingRule getRule(String cn, int delay, String... groupIDs) {
        ArchivingRule rule = new ArchivingRule();
        rule.setCommonName(cn);
        rule.setDelayAfterInstanceStored(delay);
        rule.setStorageSystemGroupIDs(groupIDs);
        return rule;
    }

    private static Attributes getAttributes(String instName, String departmentName, String modality) {
        Attributes attrs = new Attributes();
        attrs.setString(Tag.InstitutionName, VR.LO, instName);
        attrs.setString(Tag.InstitutionalDepartmentName, VR.LO, departmentName);
        attrs.setString(Tag.Modality, VR.CS, modality);
        return attrs;
    }
    
    private void checkMatchingRules(String condition, List<ArchivingRule> matched, ArchivingRules rules, ArchivingRule... expected) {
        if (matched.size() > expected.length) {
            String allMatching = promptRules("\nMatching rules:", matched, "\n   ").toString();
            for (ArchivingRule r : expected)
                matched.remove(r);
            fail("More rules found as expected! Used Condition:" + condition + 
                    promptRules("\nRegistered rules:", rules.getList(), "\n   ") + allMatching +
                    promptRules("\n\nAdditional matching rules:", matched, "\n   "));
            
        }
        for (ArchivingRule rule : expected) {
            if ( !matched.contains(rule)) {
                fail("Expected rule not found:" + promptRule(rule) + "\nUsed Condition:" + condition +
                        promptRules("\nRegistered rules:", rules.getList(), "\n   ") +
                        promptRules("\nMatching rules:", matched, "\n   "));
            }
        }
    }
    
    private StringBuilder promptRules(String msg, List<ArchivingRule> rules, String delimiter) {
        StringBuilder sb = new StringBuilder(msg);
        for (ArchivingRule rule : rules) {
            sb.append(delimiter);
            addRule(sb, rule);
        }
        return sb;
    }
    private String promptRule(ArchivingRule rule) {
        StringBuilder sb = new StringBuilder();
        return addRule(sb, rule).toString();
    }
    private StringBuilder addRule(StringBuilder sb, ArchivingRule rule) {
        sb.append(rule.getCommonName()).append(" (");
        addConditionPrompt(sb, "DeviceNames", rule.getDeviceNames());
        addConditionPrompt(sb, "AeTitles", rule.getAeTitles());
        addConditionPrompt(sb, "Modalities", rule.getModalities());
        addConditionPrompt(sb, "InstitutionNames", rule.getInstitutionNames());
        addConditionPrompt(sb, "InstitutionalDepartmentNames", rule.getInstitutionalDepartmentNames());
        return sb.append(")");
    }
    
    private void addConditionPrompt(StringBuilder sb, String conditionName, String[] condition) {
        if (condition != null && condition.length > 0) {
            sb.append(" #").append(conditionName).append(':').append(condition[0]);
            for (int i = 1 ;  i < condition.length ; i++)
                sb.append(',').append(condition[i]);
        }
    }
    
    private static Method toMethod(String methodName) {
        try {
            return ArchivingRule.class.getMethod(methodName, String[].class);
        } catch (NoSuchMethodException | SecurityException x) {
            throw new RuntimeException("Initialization failed!", x);
        }
    }
}
