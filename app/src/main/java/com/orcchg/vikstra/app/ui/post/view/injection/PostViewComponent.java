package com.orcchg.vikstra.app.ui.post.view.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.app.ui.post.view.PostViewPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {PostViewModule.class}, dependencies = {ApplicationComponent.class})
public interface PostViewComponent {

    void inject(PostViewActivity activity);

    PostViewPresenter presenter();
}
