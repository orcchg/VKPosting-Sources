package com.orcchg.vikstra.domain.interactor.base;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;

import timber.log.Timber;

/**
 * Abstract class for a Use Case (Interactor in terms of Clean Architecture).
 * This interface represents a execution unit for different use cases (this means any use case
 * in the application should implement this contract).
 *
 * @param <Result> Generic type of result on finish of execution.
 */
public abstract class UseCase<Result> implements Runnable {
    /**
     * Callback to notify when execution of this {@link UseCase} finishes.
     */
    public interface OnPostExecuteCallback<Result> {
        void onFinish(@Nullable Result values);
        void onError(Throwable e);
    }

    private final ThreadExecutor threadExecutor;
    private final PostExecuteScheduler postExecuteScheduler;
    private OnPostExecuteCallback<Result> postExecuteCallback;

    protected int orderId;  // id used to distinguish or sort different use-cases.

    /**
     * Basic construction of a {@link UseCase} class instance.
     *
     * @param threadExecutor where to push the request
     * @param postExecuteScheduler where to observe the result
     */
    protected UseCase(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        this.threadExecutor = threadExecutor;
        this.postExecuteScheduler = postExecuteScheduler;
    }

    /**
     * This ctor must be used only when this {@link UseCase} is executed synchronously within
     * some another {@link UseCase}, which must call {@link UseCase#doAction()}.
     */
    protected UseCase() {
        this.threadExecutor = null;
        this.postExecuteScheduler = null;
    }

    protected void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    protected int getOrderId() {
        return orderId;
    }

    /**
     * Sets external callback to observe the result of {@link UseCase} execution.
     *
     * @param postExecuteCallback how to process the result
     */
    public void setPostExecuteCallback(OnPostExecuteCallback<Result> postExecuteCallback) {
        this.postExecuteCallback = postExecuteCallback;
    }

    /**
     * Creates a concrete work-horse method which then will be executed in this {@link UseCase}.
     *
     * @return concrete result of execution of this {@link UseCase}.
     */
    @Nullable
    protected abstract Result doAction();

    /**
     * Execute this {@link UseCase} in it's {@link UseCase#threadExecutor} and
     * observe the result in it's {@link UseCase#postExecuteScheduler} via it's
     * {@link UseCase#postExecuteCallback}.
     */
    public void execute() {
        Timber.tag(this.getClass().getSimpleName());
        Timber.d("Executing Use-Case...");
        if (threadExecutor == null) {
            String message = "UseCase created using default ctor must only be executed" +
                    " synchronously within some another UseCase !";
            Timber.wtf(message);
            throw new IllegalStateException(message);
        }
        threadExecutor.execute(this);
    }

    /**
     * This method must not be called directly despite it's public access modifier.
     */
    @Override
    public void run() {
        try {
            Result result = doAction();
            Timber.tag(this.getClass().getSimpleName());
            Timber.d("Finished Use-Case execution");
            postExecuteScheduler.post(wrapToRunnable(result));
        } catch (Throwable error) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.e(error, "An error has occurred during execution of Use-Case");
            postExecuteScheduler.post(wrapToRunnable(error));
        }
    }

    /* Internal */
    // ------------------------------------------------------------------------
    private Runnable wrapToRunnable(final @Nullable Result result) {
        return new Runnable() {
            @Override
            public void run() {
                if (postExecuteCallback != null) postExecuteCallback.onFinish(result);
            }
        };
    }

    private Runnable wrapToRunnable(final Throwable error) {
        return new Runnable() {
            @Override
            public void run() {
                if (postExecuteCallback != null) postExecuteCallback.onError(error);
            }
        };
    }
}
