package org.dcm4chee.archive.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransactionSynchronization {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionSynchronization.class);

    private static final ThreadLocal<List<Runnable>> toRunOnCommit = new ThreadLocal<>();

    @Resource(lookup="java:jboss/TransactionManager")
    private TransactionManager tmManager;

    /**
     * Executes r once (and only if) the current transaction was successfully committed.
     * For multiple calls, the order of execution is preserved.
     * If called without a transaction - simply executes r right away
     * @param r what to execute
     */
    public void afterSuccessfulCommit(final Runnable r) {

        try {
            Transaction transaction = tmManager.getTransaction();

            if (transaction == null) {
                // if no tx - just execute the callback
                r.run();
            } else {
                // otherwise register the callback if necessary and add to the list
                if (toRunOnCommit.get() == null) {
                    toRunOnCommit.set(new ArrayList<Runnable>());
                    transaction.registerSynchronization(new OnCommitRunner());
                }
                toRunOnCommit.get().add(r);
            }
        } catch (SystemException | RollbackException e) {
            throw new RuntimeException("Cannot register on-commit hook", e);
        }
    }

    private static class OnCommitRunner implements Synchronization {
        @Override
        public void beforeCompletion() {

        }

        @Override
        public void afterCompletion(int status) {
            try {
                // run only if successfully committed
                if (status == Status.STATUS_COMMITTED) {
                    for (Runnable runnable : toRunOnCommit.get()) {
                        try {
                            runnable.run();
                        } catch (Exception e) {
                            LOG.error("Error while executing a callback after transaction commit",e);
                        }
                    }
                }
            } finally {
                toRunOnCommit.remove();
            }
        }
    }
}
