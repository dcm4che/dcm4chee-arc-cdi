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

package org.dcm4chee.archive.query.impl;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;

import org.easymock.EasyMockSupport;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

public class SessionProducerTest {
	EasyMockSupport easyMockSupport;
	EntityManager mockEntityManager;
	SessionProducer cut;
	
	@Before
	public void before() {
		easyMockSupport = new EasyMockSupport();
		mockEntityManager = easyMockSupport.createMock(EntityManager.class);
		
		cut = new SessionProducer();
		cut.em = mockEntityManager;
	}

	@Test
	public void produceSession_unwrapsHibernateSession_always() {
		Session mockSession = easyMockSupport.createNiceMock(Session.class);
		
		expect(mockEntityManager.unwrap(Session.class)).andReturn(mockSession);
		
		easyMockSupport.replayAll();
		
		assertThat(cut.produceSession(), is(sameInstance(mockSession)));
		
		easyMockSupport.verifyAll();
	}
}
