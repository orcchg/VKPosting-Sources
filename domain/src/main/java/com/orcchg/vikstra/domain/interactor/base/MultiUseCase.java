package com.orcchg.vikstra.domain.interactor.base;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class MultiUseCase<Result, L extends List<Ordered<Result>>> extends UseCase<L> {
    private int BASE_ORDER_ID = 0;

    protected int total;
    protected Class<? extends Throwable>[] allowedErrors;  // list of errors the failed use case should retry on raised
    protected final Object lock = new Object();

    public MultiUseCase(int total, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.total = total;
    }

    public void setAllowedError(Class<? extends Throwable>... allowedErrors) {
        this.allowedErrors = allowedErrors;
    }

    /* Callback */
    // ------------------------------------------
    public interface ProgressCallback {
        void onDone(int index, int total);
    }

    protected ProgressCallback progressCallback;

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected abstract List<? extends UseCase<Result>> createUseCases();

    @Nullable @Override
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
    @DebugLog
    protected <Result> List<Ordered<Result>> performMultipleRequests(final int total, final List<? extends UseCase<Result>> useCases) {
        Timber.v("Performing multiple requests, total: %s, different use-cases: %s", total, useCases.size());
        Timber.v("Allowed errors total: %s", ValueUtility.sizeOf(allowedErrors));
        final List<Ordered<Result>> results = new ArrayList<>();
        final boolean[] doneFlags = new boolean[total];
        Arrays.fill(doneFlags, false);

        for (int i = 0; i < total; ++i) {
            Timber.v("Request [%s / %s]", i + 1, total);
            final int index = i;
            final long start = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long elapsed = start;
                    Ordered<Result> result = new Ordered<>();
                    REQUEST_ATTEMPT: while (elapsed - start < 30_000) {
                        try {
                            UseCase<Result> useCase = useCases.size() == 1 ? useCases.get(0) : useCases.get(index);
                            result.orderId = useCase.getOrderId();
                            result.data = useCase.doAction();  // perform use case synchronously
                            if (progressCallback != null) progressCallback.onDone(index + 1, total);
                            break REQUEST_ATTEMPT;
                        } catch (Throwable e) {
                            if (ValueUtility.containsClass(e, allowedErrors)) {
                                // in case of any allowed error - retry after randomized timeout
                                try {
                                    long delta = ValueUtility.random(100, 1000);
                                    Thread.sleep(1000 + delta);
                                } catch (InterruptedException ie) {
                                    Thread.interrupted();  // continue executing at interruption
                                }
                                Timber.d("Retrying request...");
                            } else {
                                Timber.w("Unhandled exception: %s", e.toString());
                                result.error = e;
                                break REQUEST_ATTEMPT;
                            }
                        }
                        elapsed = System.currentTimeMillis();
                    }
                    Timber.d("Break loop");
                    addToResults(results, result);
                    synchronized (lock) {
                        doneFlags[index] = true;
                        lock.notify();  // пробудить поток-обработчик ответа
                    }
                }
            }).start();
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

    protected synchronized <Result> void addToResults(List<Ordered<Result>> results, @Nullable Ordered<Result> result) {
        if (result != null) results.add(result);
    }
}
