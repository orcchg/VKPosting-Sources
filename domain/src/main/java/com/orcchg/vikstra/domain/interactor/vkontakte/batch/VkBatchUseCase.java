package com.orcchg.vikstra.domain.interactor.vkontakte.batch;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseRetryException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKResponse;

import java.util.List;

import timber.log.Timber;

public abstract class VkBatchUseCase<Result, L extends List<Result>> extends UseCase<L> {

    protected final Object mLock = new Object();
    protected volatile boolean mDoneCondition;
    protected VKResponse[] vkBatchResponse;
    private RuntimeException vkException;

    protected VkBatchUseCase(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected VkBatchUseCase() {
        super();
    }

    protected abstract VKBatchRequest prepareVkBatchRequest();

    protected abstract L parseVkBatchResponse();

    /* Callback */
    // ------------------------------------------
    protected MultiUseCase.ProgressCallback progressCallback;

    public void setProgressCallback(MultiUseCase.ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Nullable @Override
    protected L doAction() {
        vkBatchResponse = null;
        mDoneCondition = false;

        prepareVkBatchRequest().executeWithListener(createVkResponseListener());

        synchronized (mLock) {
            while (!mDoneCondition) {
                try {
                    mLock.wait();
                    if (vkException != null) {
                        throw vkException;  // use-case has finished with error - throw exception upwards
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();  // continue executing at interruption
                }
            }
        }

        return parseVkBatchResponse();
    }

    private VKBatchRequest.VKBatchRequestListener createVkResponseListener() {
        return new VKBatchRequest.VKBatchRequestListener() {
            @Override
            public void onComplete(VKResponse[] responses) {
                super.onComplete(responses);
                synchronized (mLock) {
                    Timber.i("Successfully received batch response: %s", responsesToString(responses));
                    vkBatchResponse = responses;
                    mDoneCondition = true;
                    mLock.notify();  // wake-up use-case processing thread
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                synchronized (mLock) {
                    Timber.e("Failed to receive response: %s", error.toString());
                    if (error.apiError.errorCode == 6) {
                        Timber.d("Throwing Vk use-case retry exception");
                        vkException = new VkUseCaseRetryException();
                    } else {
                        vkException = new VkUseCaseException(error);
                    }
                    mLock.notify();  // wake-up use-case processing thread
                }
            }
        };
    }

    private String responsesToString(VKResponse[] responses) {
        String delim = "";
        StringBuilder builder = new StringBuilder("[");
        for (VKResponse response : responses) {
            builder.append(delim);
            builder.append(response.responseString).append("]");
            delim = ", [";
        }
        return builder.toString();
    }
}
