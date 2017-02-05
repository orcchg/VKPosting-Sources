package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import javax.inject.Inject;

public class DeleteWallPost extends ProcessWallPost {

    @Inject
    public DeleteWallPost(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    /**
     * For internal use within another {@link UseCase} and synchronous calls only
     */
    DeleteWallPost() {
    }

    @Override
    protected VKRequest createVkRequest(VKParameters parameters) {
        return VKApi.wall().delete(parameters);
    }
}
