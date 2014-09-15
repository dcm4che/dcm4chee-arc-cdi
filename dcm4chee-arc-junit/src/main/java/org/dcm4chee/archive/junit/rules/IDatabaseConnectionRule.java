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

package org.dcm4chee.archive.junit.rules;

import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.rules.ExternalResource;

public class IDatabaseConnectionRule extends ExternalResource {
    final EntityManagerRule entityManagerRule;

    IDatabaseConnection iDatabaseConnection;

    public IDatabaseConnectionRule(EntityManagerRule entityManagerRule) {
        this.entityManagerRule = entityManagerRule;
    }

    @Override
    protected void before() throws Throwable {
        ((Session) entityManagerRule.getEntityManager().unwrap(Session.class))
                .doWork(new Work() {
                    @Override
                    public void execute(Connection connection)
                            throws SQLException {
                        try {
                            iDatabaseConnection = new DatabaseConnection(
                                    connection);
                        } catch (DatabaseUnitException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });

        iDatabaseConnection.getConfig().setProperty(
                DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                new HsqldbDataTypeFactory());
    }

    @Override
    protected void after() {
        try {
            iDatabaseConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public IDatabaseConnection getiDatabaseConnection() {
        return iDatabaseConnection;
    }
}
