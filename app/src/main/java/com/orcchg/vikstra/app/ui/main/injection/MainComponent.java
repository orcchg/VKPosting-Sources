package com.orcchg.vikstra.app.ui.main.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.group.list.fragment.injection.GroupListModule;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListModule;
import com.orcchg.vikstra.app.ui.main.MainActivity;
import com.orcchg.vikstra.app.ui.main.MainPresenter;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridModule;

import dagger.Component;

@PerActivity
@Component(modules = {GroupListModule.class, KeywordListModule.class, MainModule.class,
                      PostModule.class, PostSingleGridModule.class},
           dependencies = {ApplicationComponent.class})
public interface MainComponent {

    void inject(MainActivity activity);

    MainPresenter presenter();
}
