package com.orcchg.vikstra.app.ui.post.single.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.injection.ListModule;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.interactor.post.GetPosts;

import dagger.Module;
import dagger.Provides;

@Module
public class PostSingleGridModule extends ListModule {

    public PostSingleGridModule(@BaseSelectAdapter.SelectMode int selectMode) {
        super(selectMode);
    }

    @Provides @PerActivity
    protected PostSingleGridPresenter providePostSingleGridPresenter(GetPosts getPostsUseCase,
            PostToSingleGridVoMapper postToSingleGridVoMapper) {
        return new PostSingleGridPresenter(selectMode, getPostsUseCase, postToSingleGridVoMapper);
    }
}
