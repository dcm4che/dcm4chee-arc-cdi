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

package org.dcm4chee.archive.query.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.QInstance;
import org.dcm4chee.archive.entity.QSeries;
import org.dcm4chee.archive.entity.Utils;
import org.dcm4chee.archive.query.QueryContext;
import org.dcm4chee.archive.query.util.QueryBuilder;
import org.dcm4chee.storage.conf.Availability;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
class InstanceQuery extends AbstractQuery<Instance> {

    private static final Expression<?>[] SELECT = {
        QSeries.series.pk,
        QInstance.instance.retrieveAETs,
        QInstance.instance.externalRetrieveAET,
        QInstance.instance.availability,
        QInstance.instance.attributesBlob.encodedAttributes
    };

    private Long seriesPk;
    private Attributes seriesAttrs;

    public InstanceQuery(QueryContext context, StatelessSession session) {
        super(context, session, QInstance.instance);
    }

    @Override
    protected Expression<?>[] select() {
        return SELECT;
    }

    @Override
    protected HibernateQuery applyJoins(HibernateQuery query) {
        query = QueryBuilder.applyInstanceLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        query = QueryBuilder.applySeriesLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        query = QueryBuilder.applyStudyLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        query = QueryBuilder.applyPatientLevelJoins(query,
                context.getKeys(),
                context.getQueryParam());
        return query;
    }

    @Override
    protected Predicate predicate() {
        BooleanBuilder builder = new BooleanBuilder();
        QueryBuilder.addPatientLevelPredicates(builder,
                context.getPatientIDs(),
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addStudyLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addSeriesLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        QueryBuilder.addInstanceLevelPredicates(builder,
                context.getKeys(),
                context.getQueryParam());
        return builder;
    }

   @Override
    public Attributes toAttributes(ScrollableResults results) {
        Long seriesPk = results.getLong(0);
        String retrieveAETs = results.getString(1);
        String externalRetrieveAET = results.getString(2);
        Availability availability = (Availability) results.get(3);
        byte[] instByteAttributes = results.getBinary(4);
        if (!seriesPk.equals(this.seriesPk)) {
            this.seriesAttrs = context.getQueryService()
                    .getSeriesAttributes(seriesPk, context.getQueryParam());
            this.seriesPk = seriesPk;
        }
        Attributes instanceAttrs = new Attributes();
        Utils.decodeAttributes(instanceAttrs, instByteAttributes);
        Attributes attrs = Utils.mergeAndNormalize(seriesAttrs, instanceAttrs);
        Utils.setRetrieveAET(attrs, retrieveAETs, externalRetrieveAET);
        Utils.setAvailability(attrs, availability);
        return attrs;
    }

}
