package com.orcchg.vikstra.domain.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class ThreadExecutor {
    protected static final int INITIAL_POOL_SIZE = 5;
    protected static final int MAX_POOL_SIZE = 8;
    protected static final int KEEP_ALIVE_TIME = 10;
    protected static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    protected ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public ThreadExecutor() {
        threadPoolExecutor = initThreadPoolExecutor();
    }

    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Runnable to execute cannot be null");
        }
        threadPoolExecutor.execute(command);
    }

    public void shutdownNow() {
        Timber.tag(getClass().getSimpleName());
        Timber.i("Shutting down now Thread Executor");
        threadPoolExecutor.shutdownNow();
        Timber.tag(getClass().getSimpleName());
        Timber.i("Re-initialize new Thread Executor");
        threadPoolExecutor = initThreadPoolExecutor();
    }

    protected static class JobThreadFactory implements ThreadFactory {
        // TODO: add identificator to thread relative ot screen
        private static final String THREAD_NAME = "vikstra_thread_";
        private int counter = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, THREAD_NAME + counter++);
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected BlockingQueue<Runnable> createWorkingQueue() {
        return new LinkedBlockingQueue<>();
    }

    // {@see http://letslearnjavaj2ee.blogspot.ru/2013/08/threadpoolexecutor-handler-policies-for.html}
    protected RejectedExecutionHandler createRejectPolicy() {
        return new ThreadPoolExecutor.DiscardPolicy();
    }

    protected ThreadFactory createThreadFactory() {
        return new JobThreadFactory();
    }

    protected ThreadPoolExecutor initThreadPoolExecutor() {
        return new ThreadPoolExecutor(INITIAL_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
                createWorkingQueue(), createThreadFactory(), createRejectPolicy());
    }
}
