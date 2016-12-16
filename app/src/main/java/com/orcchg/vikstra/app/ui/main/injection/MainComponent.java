package com.orcchg.vikstra.app.ui.main.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListModule;
import com.orcchg.vikstra.app.ui.main.MainActivity;
import com.orcchg.vikstra.app.ui.main.MainPresenter;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;

import dagger.Component;

@PerActivity
@Component(modules = {KeywordListModule.class, MainModule.class, PostSingleGridModule.class},
        dependencies = {ApplicationComponent.class})
public interface MainComponent {

    void inject(MainActivity activity);

    MainPresenter presenter();
}
