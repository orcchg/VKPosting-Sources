package com.orcchg.vikstra.domain.interactor.base;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.DomainConfig;
import com.orcchg.vikstra.domain.executor.PausableThreadPoolExecutor;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * A special kind of {@link UseCase} which is designed to execute multiple instances of {@link UseCase}
 * in parallel and accumulate total results preserving the same order as the input.
 *
 * In current implementation {@link MultiUseCase} never falls into {@link OnPostExecuteCallback#onError(Throwable)}
 * callback, it always finished with {@link OnPostExecuteCallback#onFinish(Object)} passing the list of
 * {@link Ordered} items which, in turn, could contain either valid data or error.
 */
public abstract class MultiUseCase<Result, L extends List<Ordered<Result>>> extends UseCase<L> {
    private int BASE_ORDER_ID = 0;

    protected int total;
    private AtomicBoolean isCancelled = new AtomicBoolean();
    private AtomicBoolean isSuspended = new AtomicBoolean();
    private Class<? extends Throwable>[] allowedErrors;   // if any of these errors occurs, use-case should retry execution
    private Class<? extends Throwable>[] suspendErrors;   // if any of these errors occurs, any further use-cases should wait until resumed
    private Class<? extends Throwable>[] terminalErrors;  // if any of these errors occurs, any further use-cases should be rejected
    private Throwable cancellationReason;

    private final Object lock = new Object();
    private int sleepInterval = DomainConfig.INSTANCE.multiUseCaseSleepInterval();  // to avoid Captcha error, interval in ms

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

    @DebugLog
    public void setSleepInterval(int interval) {
        sleepInterval = interval;
    }

    /* Control flow */
    // --------------------------------------------------------------------------------------------
    public void pause() {
        suspend(true);
    }

    public void resume() {
        suspend(false);
    }

    private void suspend(boolean paused) {
        if (isSuspended.get() == paused) return;  // idempotent operation

        isSuspended.getAndSet(paused);  // atomic operation
        synchronized (lock) {
            Timber.tag(getClass().getSimpleName());
            Timber.d("Toggled suspend: %s", paused);
            lock.notify();  // wake-up main thread
        }
    }

    /* Error sets */
    // --------------------------------------------------------------------------------------------
    protected void setAllowedErrors(Class<? extends Throwable>... allowedErrors) {
        this.allowedErrors = allowedErrors;
    }

    protected void setSuspendErrors(Class<? extends Throwable>... suspendErrors) {
        this.suspendErrors = suspendErrors;
    }

    protected void setTerminalErrors(Class<? extends Throwable>... terminalErrors) {
        this.terminalErrors = terminalErrors;
    }

    public Class<? extends Throwable>[] getAllowedErrors() {
        return allowedErrors;
    }

    public Class<? extends Throwable>[] getSuspendErrors() {
        return suspendErrors;
    }

    public Class<? extends Throwable>[] getTerminalErrors() {
        return terminalErrors;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
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

    // ------------------------------------------
    public interface CancelCallback {
        void onCancel(Throwable reason);
    }

    private CancelCallback cancelCallback;

    public CancelCallback getCancelCallback() {
        return cancelCallback;
    }

    public void setCancelCallback(CancelCallback cancelCallback) {
        this.cancelCallback = cancelCallback;
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
        Timber.tag(getClass().getSimpleName());
        Timber.v("Performing multiple requests, total: %s, different use-cases: %s", total, useCases.size());
        Timber.tag(getClass().getSimpleName());
        Timber.v("Allowed errors total: %s", ValueUtility.sizeOf(allowedErrors));
        final List<Ordered<Result>> results = new ArrayList<>();
        final boolean[] doneFlags = new boolean[total];
        Arrays.fill(doneFlags, false);

        // local pool designed to handle highload multi-use-case
        PausableThreadPoolExecutor threadExecutor = createHighloadThreadPoolExecutor();  // could be overriden in sub-classes

        // fire starting callback
        progressCallbackScheduler.post(new Runnable() {
            @Override
            public void run() {
                if (progressCallback != null) progressCallback.onDone(Constant.INIT_PROGRESS, Constant.INIT_PROGRESS, null);
            }
        });

        for (int i = 0; i < total; ++i) {
            // leave immediately, if main thread executor has shutdown
            if (checkInterruption()) return preparedResults(results);  // return results that have been recorded

            Timber.tag(getClass().getSimpleName());
            Timber.v("Request [%s / %s]", i + 1, total);
            final int index = i;
            final long start = System.currentTimeMillis();

            if (isCancelled.get()) {
                if (!threadExecutor.isShutdown()) {  // shutdown only once
                    Timber.tag(getClass().getSimpleName());
                    Timber.d("Execution was cancelled, skipped all iterations from %s or %s", i + 1, total);
                    threadExecutor.shutdownNow();  // interrupt all running use-cases, but don't accept the new ones
                }
                Timber.tag(getClass().getSimpleName());
                Timber.d("Skipped request [%s / %s]", i + 1, total);
                doneFlags[index] = true;  // mark cancelled use-case as already finished
                continue;  // skip not launched use-cases and go to wait for the currently running ones
            }

            if (isSuspended.get()) {  // paused
                if (!threadExecutor.isPaused()) {
                    Timber.tag(getClass().getSimpleName());
                    Timber.d("Execution has been paused");
                    threadExecutor.pause();
                    synchronized (lock) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Timber.w("Interruption during suspension");
                            Thread.currentThread().interrupt();
                            return preparedResults(results);  // return results that have been recorded
                        }
                    }
                }
            }
            if (!isSuspended.get()) {  // resumed
                if (threadExecutor.isPaused()) {
                    Timber.tag(getClass().getSimpleName());
                    Timber.d("Execution has been resumed");
                    threadExecutor.resume();
                }
            }

            Timber.tag(getClass().getSimpleName());
            Timber.v("Executing...");
            threadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    long elapsed = start;
                    final Ordered<Result> result = new Ordered<>();
                    while (elapsed - start < 15_000 && !Thread.currentThread().isInterrupted()) {
                        try {
                            Timber.tag(getClass().getSimpleName());
                            Timber.v("Performing request [%s] at time %s", index + 1, ValueUtility.time());
                            UseCase<Result> useCase = useCases.size() == 1 ? useCases.get(0) : useCases.get(index);
                            result.orderId = useCase.getOrderId();
                            result.parameters = useCase.getInputParameters();  // supply input parameters of use-case
                            result.data = useCase.doAction();  // perform use-case synchronously
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
                                    /**
                                     * Blocking method {@link Thread#sleep(long)} has been interrupted
                                     * and this method clears 'interrupted' flag of the current thread.
                                     * Restore 'interrupted' flag and continue executing while-loop
                                     * checking whether the current thread was interrupted.
                                     */
                                    Timber.tag(getClass().getSimpleName());
                                    Timber.d("Sleeping on retry use-case has been interrupted, probably cancelled");
                                    Thread.currentThread().interrupt();
                                }
                                Timber.tag(getClass().getSimpleName());
                                Timber.v("Retrying request [%s]...", index + 1);
                            } else {
                                // test terminal error as soon as possible and proceed
                                if (ValueUtility.containsClass(e, terminalErrors)) {
                                    isCancelled.getAndSet(true);  // atomic operation
                                    setCancellationReason(e);  // assign proper reason of cancellation
                                    Timber.tag(getClass().getSimpleName());
                                    Timber.d("Terminal error has occurred - cancel the rest use-cases");
                                } else if (ValueUtility.containsClass(e, suspendErrors)) {
                                    // suspend errors are less important than terminal errors
                                    isSuspended.getAndSet(true);  // atomic operation
                                    Timber.tag(getClass().getSimpleName());
                                    Timber.d("Suspend error has occurred - pause the rest use-cases");
                                }

                                Timber.tag(getClass().getSimpleName());
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
                    Timber.tag(getClass().getSimpleName());
                    Timber.v("Finished while-loop");
                    if (result.data == null && result.error == null) {
                        Timber.tag(getClass().getSimpleName());
                        Timber.v("Current use-case [%s / %s] has failed to retry over allowed time %s",
                                index + 1, total, "it is marked cancelled now");
                        result.cancelled = true;  // this use-case has failed to retry over time
                    }

                    if (Thread.currentThread().isInterrupted() && isCancelled.get()) {
                        Timber.tag(getClass().getSimpleName());
                        Timber.d("Use-case has been interrupted due to it's cancellation");
                        result.cancelled = true;  // add cancelled result to the output collection
                    }

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
                    Thread.currentThread().interrupt();  // in case of any allowed error - continue iterating
                }
            }
        }

        /**
         * Waiting for all use-cases to finish. Even if some use-cases were cancelled, their results
         * will anyway be recorded to preserve correct ordering and correspondence between input use-cases
         * and output results.
         */
        Timber.tag(getClass().getSimpleName());
        Timber.v("All tasks have been sent. Awaiting them to finish...");
        synchronized (lock) {
            // wait for all use-cases to finish or leave immediately, if main thread executor has shutdown
            while (!ValueUtility.isAllTrue(doneFlags) && !checkInterruption()) {
                try {
                    lock.wait();  // waiting all tasks to finish, releasing lock

                    if (isCancelled.get() && !threadExecutor.isShutdown()) {  // shutdown only once
                        Timber.tag(getClass().getSimpleName());
                        Timber.d("Execution was cancelled, interrupting all running (not finished) use-cases");
                        threadExecutor.shutdownNow();  // interrupt all running use-cases
                    }

                    if (isSuspended.get()) {  // paused
                        if (!threadExecutor.isPaused()) {
                            Timber.tag(getClass().getSimpleName());
                            Timber.d("Execution has been paused");
                            threadExecutor.pause();
                        }
                    }
                    if (!isSuspended.get()) {  // resumed
                        if (threadExecutor.isPaused()) {
                            Timber.tag(getClass().getSimpleName());
                            Timber.d("Execution has been resumed");
                            threadExecutor.resume();
                        }
                    }
                    // continue awaiting for currently running use-cases
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // in case of any allowed error - continue iterating
                }
            }
        }

        if (isCancelled.get()) progressCallbackScheduler.post(new Runnable() {
            @Override
            public void run() {
                if (cancelCallback != null) cancelCallback.onCancel(cancellationReason);
            }
        });

        return preparedResults(results);
    }

    private synchronized void addToResults(List<Ordered<Result>> results, @Nullable Ordered<Result> result) {
        if (result != null) results.add(result);
    }

    private synchronized void setCancellationReason(Throwable reason) {
        cancellationReason = reason;
    }

    private List<Ordered<Result>> preparedResults(List<Ordered<Result>> results) {
        Timber.tag(getClass().getSimpleName());
        Timber.v("Multi Use-Case: total results: %s", results.size());
        Collections.sort(results);  // sort results to correspond to each use-case
        return results;
    }

    /* Thread pool */
    // --------------------------------------------------------------------------------------------
    protected PausableThreadPoolExecutor createHighloadThreadPoolExecutor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        PausableThreadPoolExecutor pool = new PausableThreadPoolExecutor(3, 3, 10, TimeUnit.SECONDS, queue);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
}
