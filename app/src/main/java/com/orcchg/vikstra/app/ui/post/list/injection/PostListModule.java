package com.orcchg.vikstra.app.ui.post.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.post.list.PostListPresenter;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.post.DeletePost;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class PostListModule extends PostSingleGridModule {

    public PostListModule(@BaseSelectAdapter.SelectMode int selectMode) {
        super(selectMode);
    }

    @Provides @PerActivity
    protected PostListPresenter providePostListPresenter(@Named("PostGridScreen") GetPostById getPostByIdUseCase, GetPosts getPostsUseCase,
            DeletePost deletePostUseCase, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        return new PostListPresenter(selectMode, getPostByIdUseCase, getPostsUseCase, deletePostUseCase, postToSingleGridVoMapper);
    }
}
