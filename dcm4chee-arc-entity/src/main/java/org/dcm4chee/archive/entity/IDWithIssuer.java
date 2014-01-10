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

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class IDWithIssuer {

    public static final IDWithIssuer[] EMPTY = {};

    private final String id;
    private Issuer issuer;

    public IDWithIssuer(String id, Issuer issuer) {
        this.id = id;
        this.setIssuer(issuer);
    }

    public final String getID() {
        return id;
    }

    public final Issuer getIssuer() {
        return issuer;
    }

    public final void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        return getIssuer() == null ? id : id + "^^^" + getIssuer().toString('&');
    }

    public Attributes toPatientIDWithIssuer(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(3);

        attrs.setString(Tag.PatientID, VR.LO, id);
        if (getIssuer() == null)
            return attrs;

        return getIssuer().toIssuerOfPatientID(attrs);
    }

    public static IDWithIssuer valueOf(Attributes attrs, int idTag,
            int issuerSeqTag) {
        String id = attrs.getString(idTag);
        if (id == null)
            return null;

        Issuer issuer = null;
        Attributes issuerItem = attrs.getNestedDataset(issuerSeqTag);
        if (issuerItem != null && !issuerItem.isEmpty())
            issuer = new Issuer(issuerItem);

        return new IDWithIssuer(id, issuer);
    }

    public static IDWithIssuer fromPatientIDWithIssuer(Attributes attrs) {
        String id = attrs.getString(Tag.PatientID);
        if (id == null)
            return null;

        Issuer issuer = null;
        String issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID);
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        if (issuerOfPatientID != null || (qualifiers != null && !qualifiers.isEmpty()))
            issuer = new Issuer(issuerOfPatientID, qualifiers);

        return new IDWithIssuer(id, issuer);
    }

}
