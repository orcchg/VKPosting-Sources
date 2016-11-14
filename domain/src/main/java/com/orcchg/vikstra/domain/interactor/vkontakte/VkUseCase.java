package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public abstract class VkUseCase<Result> extends UseCase<Result> {

    protected VKResponse vkResponse;

    protected VkUseCase(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected abstract VKRequest prepareVkRequest();

    protected abstract Result parseVkResponse();

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected Result doAction() {
        prepareVkRequest().executeSyncWithListener(createVkResponseListener());
        return parseVkResponse();
    }

    VKRequest.VKRequestListener createVkResponseListener() {
        return new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                vkResponse = response;
            }
        };
    }
}
