package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseExceptionFactory;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseRetryException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import timber.log.Timber;

public abstract class VkUseCase<Result> extends UseCase<Result> {

    protected VKResponse vkResponse;
    private final Object lock = new Object();
    private volatile boolean doneCondition;
    private RuntimeException vkException;

    protected VkUseCase(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected VkUseCase() {
        super();
    }

    protected abstract VKRequest prepareVkRequest();

    protected abstract Result parseVkResponse();

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Nullable @Override
    protected Result doAction() {
        vkResponse = null;
        doneCondition = false;

        prepareVkRequest().executeWithListener(createVkResponseListener());

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

        return parseVkResponse();
    }

    private VKRequest.VKRequestListener createVkResponseListener() {
        return new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                synchronized (lock) {
                    Timber.tag(getClass().getSimpleName());
                    Timber.i("Successfully received response: %s", response.responseString);
                    vkResponse = response;
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
                    if (error.apiError != null && error.apiError.errorCode == 6) {
                        Timber.tag(getClass().getSimpleName());
                        Timber.d("Throwing Vk use-case retry exception");
                        vkException = new VkUseCaseRetryException();
                    } else {
                        Timber.tag(getClass().getSimpleName());
                        Timber.d("Throwing other Vk use-case exception");
                        vkException = VkUseCaseExceptionFactory.create(error);
                    }
                    lock.notify();  // wake-up use-case processing thread
                }
            }
        };
    }
}
