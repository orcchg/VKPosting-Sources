package com.orcchg.vikstra.domain.interactor.vkontakte.batch;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseException;
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

    protected final Object lock = new Object();
    protected volatile boolean doneCondition;
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
        doneCondition = false;

        prepareVkBatchRequest().executeWithListener(createVkResponseListener());

        synchronized (lock) {
            while (!doneCondition) {
                try {
                    lock.wait();
                    if (vkException != null) {
                        throw vkException;  // use-case has finished with error - throw exception upwards
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // continue executing at interruption
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
                synchronized (lock) {
                    Timber.tag(getClass().getSimpleName());
                    Timber.i("Successfully received batch response: %s", responsesToString(responses));
                    vkBatchResponse = responses;
                    doneCondition = true;
                    lock.notify();  // wake-up use-case processing thread
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                synchronized (lock) {
                    Timber.tag(getClass().getSimpleName());
                    Timber.e("Failed to receive response: %s", error.toString());
                    vkException = new VkUseCaseException(error);
                    lock.notify();  // wake-up use-case processing thread
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
