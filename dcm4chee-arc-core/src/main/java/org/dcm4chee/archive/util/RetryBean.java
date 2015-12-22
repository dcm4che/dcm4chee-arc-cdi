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

package org.dcm4chee.archive.util;

import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Status;

import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.util.TransactionSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry mechanism for transactional database updates.
 *
 * A retry is typically necessary if a database update fails due to an OptimisticLockException or
 * ConstraintViolationException caused by a concurrent transaction.
 *
 * @author Umberto Cappellini
 */
@Dependent
public class RetryBean<T, E extends Exception> {

    private static Logger log = LoggerFactory.getLogger(RetryBean.class);

    @Inject 
    private Device device;

    @Inject
    TransactionSynchronization transaction;

    public T retry(Retryable<T,E> callable) throws E {
        ArchiveDeviceExtension dE = device.getDeviceExtension(ArchiveDeviceExtension.class);

        int updateDbRetries = dE.getUpdateDbRetries();

        // ONLY retry if we are outside a transaction by default
        if (transaction.getStatus() != Status.STATUS_NO_TRANSACTION) {
            updateDbRetries = 1;
            log.debug("Not using a retry, because the service is called within an existing transaction");
        }

        return new RetryTask<T,E>(updateDbRetries, dE.getUpdateDbDelay(), callable).call();
    }

    /**
     * Determines if an exception is retry-able by inspecting the cause-stack.
     * @param t
     * @return Returns <code>true</code> if the exception is considered retry-able,
     * returns <code>false</code> otherwise.
     */
    public static boolean isRetryable(Throwable t) {
        if((t instanceof EJBException) && t.getCause() instanceof PersistenceException)
            return true;

        return false;
    }

    public interface Retryable<T,E extends Exception> extends Callable<T> {
        @Override
        T call() throws E;
    }
}
