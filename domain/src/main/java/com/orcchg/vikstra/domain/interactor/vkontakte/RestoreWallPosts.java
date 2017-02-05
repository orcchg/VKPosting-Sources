package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;

import javax.inject.Inject;

public class RestoreWallPosts extends ProcessWallPosts {

    @Inject
    public RestoreWallPosts(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    @Override
    protected ProcessWallPost createProcessWallPostUseCase() {
        return new RestoreWallPost();
    }
}
