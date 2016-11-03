package com.orcchg.vikstra.app.ui.keyword.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListFragment;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {KeywordListModule.class}, dependencies = {ApplicationComponent.class})
public interface KeywordListComponent {

    void inject(KeywordListFragment fragment);

    KeywordListPresenter presenter();
}
