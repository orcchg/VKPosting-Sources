package com.orcchg.vikstra.app.ui.post.single.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridFragment;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {PostSingleGridModule.class}, dependencies = {ApplicationComponent.class})
public interface PostSingleGridComponent {

    void inject(PostSingleGridFragment fragment);

    PostSingleGridPresenter presenter();
}
