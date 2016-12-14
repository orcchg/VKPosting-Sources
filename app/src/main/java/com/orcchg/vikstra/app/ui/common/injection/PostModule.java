package com.orcchg.vikstra.app.ui.common.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class PostModule {

    protected final long postId;

    protected PostModule(long postId) {
        this.postId = postId;
    }

    @Provides @PerActivity
    protected GetPostById provideGetPostById(IPostRepository repository,
             ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetPostById(postId, repository, threadExecutor, postExecuteScheduler);
    }
}
