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

package org.dcm4che.archive.store.scu.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che3.net.service.InstanceLocator;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class StoreScuJMSClient {

    private static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL = "remote://localhost:4447";
    private static final String STORE_DESTINATION = "jms/queue/storescu";
    
    // Application user/pass created on jboss/wildfly through add-user.sh
    private static final String USERNAME = "jmsuser";
    private static final String PASSWORD = "ABCDEFG1*";    

    public static void main(String[] args) {

        Context namingContext = null;
        try {

            // Set up the namingContext for the JNDI lookup
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_PRINCIPAL, USERNAME);
            env.put(Context.SECURITY_CREDENTIALS, PASSWORD);
            namingContext = new InitialContext(env);

            // Perform the JNDI lookups
            System.out.println("Attempting to acquire connection factory \""
                    + CONNECTION_FACTORY + "\"");
            ConnectionFactory connectionFactory = (ConnectionFactory) namingContext
                    .lookup(CONNECTION_FACTORY);
            System.out.println("Found connection factory \""
                    + CONNECTION_FACTORY + "\" in JNDI");
            System.out.println("Attempting to acquire destination \""
                    + STORE_DESTINATION + "\"");
            Destination destination = (Destination) namingContext
                    .lookup(STORE_DESTINATION);
            System.out.println("Found destination \"" + STORE_DESTINATION
                    + "\" in JNDI");

            Connection conn = connectionFactory.createConnection(USERNAME,
                    PASSWORD);
            Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = sess.createProducer(destination);
            producer.send(createMessage(sess));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (namingContext != null) {
                try {
                    namingContext.close();
                } catch (NamingException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * modify following values to fit the desired testing scenario
     */
    private static Message createMessage(Session session) throws JMSException {
        ObjectMessage msg = session.createObjectMessage();
        msg.setStringProperty("LocalAET", "DCM4CHEE");
        msg.setStringProperty("RemoteAET", "STORESCP2");
        msg.setIntProperty("Priority", 0);
        msg.setIntProperty("Retries", 0);
        msg.setObject((Serializable) getObject());
        return msg;
    }

    /**
     * modify following values to fit the desired testing scenario
     */
    private static List<InstanceLocator> getObject() {
        ArrayList<InstanceLocator> insts = new ArrayList<InstanceLocator>();
        InstanceLocator loc1 = new InstanceLocator("1.2.840.10008.5.1.4.1.1.1",
                "1.2.392.200036.9125.0.19950720105640", "1.2.840.10008.1.2",
                "file:///home/umberto/tmp/2015/03/06/27B3738F/27B3738F/957CEAE4aaaa");
        InstanceLocator loc2 = new InstanceLocator("1.2.840.10008.5.1.4.1.1.1",
                "1.2.392.200036.9125.0.19950720105315", "1.2.840.10008.1.2",
                "file:///home/umberto/tmp/2015/03/06/DD074A6F/DD074A6F/957CDF49");
        insts.add(loc1);
        insts.add(loc2);
        return insts;
    }

}
