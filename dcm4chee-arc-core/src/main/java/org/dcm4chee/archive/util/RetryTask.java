package org.dcm4chee.archive.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Created by umberto on 9/30/15.
 */
public class RetryTask<T, E extends Exception> implements Callable<T> {

    static Logger LOG = LoggerFactory.getLogger(RetryTask.class);

    private Callable<T> task;

    private int retries; // number of tries
    private int left; // left tries
    private long wait; // wait interval

    public RetryTask(int numberOfRetries, long timeToWait, Callable<T> task) {
        this.retries = numberOfRetries;
        left = numberOfRetries;
        this.wait = timeToWait;
        this.task = task;
    }

    public T call() throws E {
        while (true) {
            try {
                return task.call();
            }
            catch (Exception e) {
                left--;
                if (left == 0) {
                    LOG.error("task failed " + retries + " times at " + wait + "ms interval", e);
                    if (e instanceof RuntimeException)
                        throw (RuntimeException)e;
                    else
                        throw (E)e;
                }
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread Interrupted", e1);
                }
            }
        }
    }

}
