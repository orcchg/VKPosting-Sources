package com.orcchg.vikstra.app.ui.post.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.post.list.PostListActivity;
import com.orcchg.vikstra.app.ui.post.list.PostListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {PostListModule.class}, dependencies = {ApplicationComponent.class})
public interface PostListComponent {

    void inject(PostListActivity activity);

    PostListPresenter presenter();
}
