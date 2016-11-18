package com.orcchg.vikstra.domain.interactor;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.BundledException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiUseCase<Result, L extends List<Result>> extends UseCase<L> {

    protected int total;
    protected List<Throwable> errors = new ArrayList<>();  // occurred disallowed errors during execution
    protected List<Throwable> allowedErrors;  // list of errors the failed use case should retry on raised
    protected Object lock = new Object();

    public MultiUseCase(int total, ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.total = total;
    }

    public void setAllowedError(List<Throwable> allowedErrors) {
        this.allowedErrors = allowedErrors;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected abstract List<? extends UseCase<Result>> createUseCases();

    @Override
    protected L doAction() {
        List<? extends UseCase<Result>> useCases = createUseCases();
        return (L) performMultipleRequests(total, useCases, errors);
    }

    /**
     * Performs {@param total} use-cases synchronously but each in a background thread,
     * then waits them to finish and accumulates results and possible errors in lists.
     */
    protected <Result> List<Result> performMultipleRequests(int total, final List<? extends UseCase<Result>> useCases,
                                                            final List<Throwable> errors) {
        final List<Result> results = new ArrayList<>();
        final boolean[] doneFlags = new boolean[total];
        Arrays.fill(doneFlags, false);

        for (int i = 0; i < total; ++i) {
            final int index = i;
            final long start = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long elapsed = start;
                    boolean finishedWithError = false;
                    Result result = null;
                    while (elapsed - start < 30_000) {
                        try {
                            UseCase<Result> useCase = useCases.size() == 1 ? useCases.get(0) : useCases.get(index);
                            result = useCase.doAction();  // perform use case synchronously
                        } catch (Throwable e) {
                            if (allowedErrors != null && !allowedErrors.isEmpty() && allowedErrors.contains(e)) {
                                // in case of any allowed error - retry after randomized timeout
                                try {
                                    long delta = ValueUtility.random(100, 1000);
                                    Thread.sleep(1000 + delta);
                                } catch (InterruptedException ie) {
                                    Thread.interrupted();  // continue executing at interruption
                                }
                            } else {
                                addToErrors(errors, e);
                                finishedWithError = true;
                                break;
                            }
                        }
                        elapsed = System.currentTimeMillis();
                    }
                    if (!finishedWithError) {
                        addToResults(results, result);
                    }
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
                    if (!errors.isEmpty()) {
                        /**
                         * Обработка списка исключений, которые не удалось обработать в рабочих потоках выше.
                         * Здесь - передача исключения "наверх", на вызывающий данную команду клиент.
                         */
                        throw new BundledException.Builder().addErrors(errors).build();
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();  // в случае прерывания - продолжить выполнение цикла
                }
            }
        }

        return results;
    }

    protected synchronized <Result> void addToResults(List<Result> results, @Nullable Result result) {
        if (result != null) results.add(result);
    }

    protected synchronized void addToErrors(List<Throwable> errors, @Nullable Throwable error) {
        if (error != null) errors.add(error);
    }
}
