//
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

package org.dcm4chee.archive.sc.impl;

import static java.lang.String.format;




import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.dcm4chee.archive.sc.StructuralChangeContainer;
import org.dcm4chee.archive.sc.StructuralChangeTransactionHook;
import org.dcm4chee.archive.task.executor.impl.PlatformTaskExecutor;
import org.dcm4chee.archive.task.executor.impl.PlatformTaskExecutor.CompletionHandler;
import org.dcm4chee.hooks.Hooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 *
 */
@ApplicationScoped
public class StructuralChangeHookExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(StructuralChangeHookExecutor.class);
    
    @Inject
    private Hooks<StructuralChangeTransactionHook> structuralChangeHooks;
    
    @Inject
    private PlatformTaskExecutor taskExecutor;
    
    public boolean executeBeforeCommitStructuralChangeHooks(StructuralChangeContainer changeContainer) {
       
        for (StructuralChangeTransactionHook scHook : structuralChangeHooks) {
            try {
                if (!scHook.beforeCommitStructuralChanges(changeContainer)) {
                    LOG.info("Structural change hook {} marked transaction as failed", scHook.getClass().getName());
                    return false;
                }
            } catch (Exception e) {
                LOG.error(format("Error while executing BEFORE-COMMIT structural change hook %s. Mark transaction as FAILED",
                        scHook.getClass().getName()), e);
                return false;
            }
        }
       
        return true;
    }
    
    public void asyncExecuteAfterCommitStructuralChangeHooks(final StructuralChangeContainer changeContainer) {
        final CompletionHandler<String> completionHandler = new CompletionHandler<String>() {

            @Override
            public void onComplete(String hookName) {
                //NOP
            }

            @Override
            public void onException(String hookName, Exception e) {
                LOG.error(format("Error while executing AFTER-COMMIT structural change hook %s", hookName), e);
            }
        };
        
        for (final StructuralChangeTransactionHook scHook : structuralChangeHooks) {
            taskExecutor.asyncExecute(scHook.getClass().getName(), new Runnable() {
                @Override
                public void run() {
                    scHook.afterCommitStructuralChanges(changeContainer);
                }    
            }, completionHandler);
        }
    }
    
}
