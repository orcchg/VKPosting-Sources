package com.orcchg.vikstra.domain.interactor.base;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.DomainConfig;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class MultiUseCase<Result, L extends List<Ordered<Result>>> extends UseCase<L> {
    private int BASE_ORDER_ID = 0;

    protected int total;
    protected Class<? extends Throwable>[] allowedErrors;  // list of errors the failed use case should retry on raised
    protected final Object lock = new Object();
    private int sleepInterval = DomainConfig.INSTANCE.multiUseCaseSleepInterval;  // to avoid Captcha error, interval in ms

    private ThreadPoolExecutor threadExecutor;  // local pool designed to handle highload multi-use-case
    private final PostExecuteScheduler progressCallbackScheduler;  // where to observe progress callbacks

    public MultiUseCase(int total, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        this(total, threadExecutor, postExecuteScheduler, postExecuteScheduler);
    }

    public MultiUseCase(int total, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler,
                        PostExecuteScheduler progressCallbackScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.total = total;
        this.progressCallbackScheduler = progressCallbackScheduler;
    }

    public void setAllowedError(Class<? extends Throwable>... allowedErrors) {
        this.allowedErrors = allowedErrors;
    }

    @DebugLog
    public void setSleepInterval(int interval) {
        sleepInterval = interval;
    }

    /* Callback */
    // ------------------------------------------
    public interface ProgressCallback<Data> {
        void onDone(int index, int total, Ordered<Data> data);
    }

    private ProgressCallback progressCallback;

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected abstract List<? extends UseCase<Result>> createUseCases();

    @Nullable @Override @SuppressWarnings("unchecked")
    protected L doAction() {
        List<? extends UseCase<Result>> useCases = createUseCases();
        for (UseCase<Result> useCase : useCases) {
            useCase.setOrderId(BASE_ORDER_ID++);  // maintain initial order of use-cases
        }
        return (L) performMultipleRequests(total, useCases);
    }

    /**
     * Performs {@param total} use-cases synchronously but each in a background thread,
     * then waits them to finish and accumulates results and possible errors in lists.
     */
    @DebugLog @SuppressWarnings("unchecked")
    private List<Ordered<Result>> performMultipleRequests(final int total, final List<? extends UseCase<Result>> useCases) {
        Timber.tag(this.getClass().getSimpleName());
        Timber.v("Performing multiple requests, total: %s, different use-cases: %s", total, useCases.size());
        Timber.tag(this.getClass().getSimpleName());
        Timber.v("Allowed errors total: %s", ValueUtility.sizeOf(allowedErrors));
        final List<Ordered<Result>> results = new ArrayList<>();
        final boolean[] doneFlags = new boolean[total];
        Arrays.fill(doneFlags, false);

        this.threadExecutor = createHighloadThreadPoolExecutor();  // could be overriden in sub-classes

        for (int i = 0; i < total; ++i) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.v("Request [%s / %s]", i + 1, total);
            final int index = i;
            final long start = System.currentTimeMillis();
            threadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    long elapsed = start;
                    final Ordered<Result> result = new Ordered<>();
                    while (elapsed - start < 30_000) {
                        try {
                            Timber.tag(this.getClass().getSimpleName());
                            Timber.v("Performing request [%s] at time %s", index, ValueUtility.time());
                            UseCase<Result> useCase = useCases.size() == 1 ? useCases.get(0) : useCases.get(index);
                            result.orderId = useCase.getOrderId();
                            result.data = useCase.doAction();  // perform use case synchronously
                            progressCallbackScheduler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressCallback != null) progressCallback.onDone(index, total, result);
                                }
                            });
                            break;
                        } catch (Throwable e) {
                            if (ValueUtility.containsClass(e, allowedErrors)) {
                                // in case of any allowed error - retry after randomized timeout
                                try {
                                    long delta = ValueUtility.random(100, 1000);
                                    Thread.sleep(1000 + delta);
                                } catch (InterruptedException ie) {
                                    Thread.interrupted();  // continue executing at interruption
                                }
                                Timber.tag(this.getClass().getSimpleName());
                                Timber.v("Retrying request [%s]...", index);
                            } else {
                                Timber.tag(this.getClass().getSimpleName());
                                Timber.w("Unhandled exception: %s", e.toString());
                                result.error = e;
                                progressCallbackScheduler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressCallback != null) progressCallback.onDone(index, total, result);
                                    }
                                });
                                break;
                            }
                        }
                        elapsed = System.currentTimeMillis();
                    }
                    Timber.tag(this.getClass().getSimpleName());
                    Timber.v("Break loop");
                    addToResults(results, result);
                    synchronized (lock) {
                        doneFlags[index] = true;
                        lock.notify();  // wake-up main thread
                    }
                }
            });

            // optional pause before starting next use-case execution
            if (total > 1 && sleepInterval > 0) {
                try {
                    Thread.sleep(sleepInterval);
                } catch (InterruptedException e) {
                    Thread.interrupted();  // continue executing at interruption
                }
            }
        }

        synchronized (lock) {
            while (!ValueUtility.isAllTrue(doneFlags)) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.interrupted();  // в случае прерывания - продолжить выполнение цикла
                }
            }
        }

        Collections.sort(results);  // sort results to correspond to each use-case
        return results;
    }

    private synchronized void addToResults(List<Ordered<Result>> results, @Nullable Ordered<Result> result) {
        if (result != null) results.add(result);
    }

    /* Thread pool */
    // --------------------------------------------------------------------------------------------
    protected ThreadPoolExecutor createHighloadThreadPoolExecutor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 3, 10, TimeUnit.SECONDS, queue);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
}
