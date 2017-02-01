package com.orcchg.vikstra.app.ui.post.single.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.injection.ListModule;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.post.DeletePost;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class PostSingleGridModule extends ListModule {

    public PostSingleGridModule(@BaseSelectAdapter.SelectMode int selectMode) {
        super(selectMode);
    }

    @Provides @PerActivity @Named("PostGridScreen")
    protected GetPostById provideGetPostById(IPostRepository repository,
            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        /**
         * Creates {@link GetPostById} with {@link Constant.BAD_ID} because this use case will have
         * it's {@link GetPostById#id} set inside the {@link PostSingleGridPresenter}.
         */
        return new GetPostById(Constant.BAD_ID, repository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected PostSingleGridPresenter providePostSingleGridPresenter(@Named("PostGridScreen") GetPostById getPostByIdUseCase,
            GetPosts getPostsUseCase, DeletePost deletePostUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        return new PostSingleGridPresenter(selectMode, getPostByIdUseCase, getPostsUseCase, deletePostUseCase, postToSingleGridVoMapper);
    }
}
