/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.threadpool
 * File         : ThreadPool.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.threadpool;

import blackberry.utils.BlockingQueue;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;

// TODO: Auto-generated Javadoc
/**
 * implementation of a thread pool.
 * 
 * @author thanks to: Rob Gordon.
 */
public final class ThreadPool {

    /**
     * The Class PoolClosedException.
     */
    static class PoolClosedException extends RuntimeException {

        /**
         * Instantiates a new pool closed exception.
         */
        PoolClosedException() {
            super("Pool closed.");
        }
    }

    private class PooledThread extends Thread {

        int id;

        public PooledThread(final int i) {
            id = i;
        }

        public void run() {
            while (true) {
                final Runnable job = (Runnable) queue.dequeue();

                if (job == null) {
                    break;
                }

                try {
                    //#debug debug
                    debug.trace("Pool " + id + " run:" + job);
                    job.run();
                    //#debug debug
                    debug.trace("Pool " + id + " end:" + job);
                } catch (final Throwable t) {
                    // ignore
                }
            }
        }
    }

    //#debug
    static Debug debug = new Debug("ThreadPool", DebugLevel.VERBOSE);

    protected final BlockingQueue queue = new BlockingQueue();

    protected boolean closed = true;

    private int poolSize;

    /**
     * Instantiates a new thread pool.
     * 
     * @param numThreads
     *            the num threads
     */
    public ThreadPool(final int numThreads) {
        poolSize = numThreads;
    }

    /**
     * Close.
     */
    public void close() {
        closed = true;
        queue.close();
    }

    /**
     * Execute.
     * 
     * @param job
     *            the job
     */
    public synchronized void execute(final Runnable job) {
        if (closed) {
            throw new PoolClosedException();
        }
        queue.enqueue(job);
    }

    /**
     * Gets the pool size.
     * 
     * @return the pool size
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Sets the pool size.
     * 
     * @param poolSize_
     *            the new pool size
     */
    public void setPoolSize(final int poolSize_) {
        poolSize = poolSize_;
    }

    /**
     * Start.
     */
    public synchronized void start() {
        if (!closed) {
            throw new IllegalStateException("Pool already started.");
        }
        closed = false;
        for (int i = 0; i < poolSize; ++i) {
            new PooledThread(i).start();
        }
    }

}

/*
 * Copyright ? 2004, Rob Gordon.
 */
