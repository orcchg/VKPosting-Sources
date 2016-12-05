package com.orcchg.vikstra.app.ui.post.create.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.create.PostCreatePresenter;

import dagger.Component;

@PerActivity
@Component(modules = {PostCreateModule.class}, dependencies = {ApplicationComponent.class})
public interface PostCreateComponent {

    void inject(PostCreateActivity activity);

    PostCreatePresenter presenter();
}
