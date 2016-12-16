package com.orcchg.vikstra.app.ui.post.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.common.injection.ListModule;
import com.orcchg.vikstra.app.ui.post.list.PostListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;

import dagger.Module;
import dagger.Provides;

@Module
public class PostListModule extends ListModule {

    public PostListModule(@BaseAdapter.SelectMode int selectMode) {
        super(selectMode);
    }

    @Provides @PerActivity
    protected PostListPresenter providePostListPresenter(GetPosts getPostsUseCase,
             PostToSingleGridVoMapper postToSingleGridVoMapper) {
        return new PostListPresenter(selectMode, getPostsUseCase, postToSingleGridVoMapper);
    }
}
