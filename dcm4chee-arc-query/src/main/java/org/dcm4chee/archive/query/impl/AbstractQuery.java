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

import java.util.NoSuchElementException;

import org.dcm4che3.data.Attributes;
import org.dcm4chee.archive.query.Query;
import org.dcm4chee.archive.query.QueryContext;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;

import com.mysema.query.jpa.hibernate.HibernateQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 */
public abstract class AbstractQuery<E> implements Query {

    protected final QueryContext context;

    protected final StatelessSession session;

    protected ScrollableResults results;

    protected HibernateQuery query;

    private boolean hasMoreMatches;
 
    private boolean optionalKeyNotSupported;

    private final EntityPath<E> entityPath;

    public AbstractQuery(QueryContext context, StatelessSession session,
            EntityPath<E> entityPath) {
        this.context = context;
        this.session = session;
        this.entityPath = entityPath;
    }

    @Override
    public QueryContext getQueryContext() {
        return context;
    }

    @Override
    public void initQuery() {
        HibernateQuery q = new HibernateQuery(session).from(entityPath);
        q = applyJoins(q);
        query = q.where(predicate());
    }

    protected abstract Expression<?>[] select();

    protected abstract HibernateQuery applyJoins(HibernateQuery q);

    protected abstract Predicate predicate();

    protected abstract Attributes toAttributes(ScrollableResults results, QueryContext context);

    @Override
    public void executeQuery() {
        results = query.scroll(ScrollMode.FORWARD_ONLY, select());
        hasMoreMatches = results.next();
    }

    @Override
    public long count() {
        checkQuery();
        return query.count();
    }

    @Override
    public void limit(long limit) {
        checkQuery();
        query.limit(limit);
    }

    @Override
    public void offset(long offset) {
        checkQuery();
        query.offset(offset);
    }

    @Override
    public void orderBy(OrderSpecifier<?>... orderSpecifiers) {
        checkQuery();
        query.orderBy(orderSpecifiers);
    }

    @Override
    public boolean optionalKeysNotSupported() {
        return optionalKeyNotSupported;
    }

    @Override
    public boolean hasMoreMatches() {
        return hasMoreMatches;
    }

    @Override
    public Attributes nextMatch() {
        if (!hasMoreMatches)
            throw new NoSuchElementException();
        Attributes attrs = toAttributes(results, context);
        hasMoreMatches = results.next();
        return attrs;
    }

    private void checkQuery() {
        if (query == null)
            throw new IllegalStateException("query not initalized");
    }

    @Override
    public void close() {
        session.close();
    }

}
