package com.orcchg.vikstra.app.ui.keyword.create.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreatePresenter;

import dagger.Component;

@PerActivity
@Component(modules = {KeywordCreateModule.class}, dependencies = {ApplicationComponent.class})
public interface KeywordCreateComponent {

    void inject(KeywordCreateActivity activity);

    KeywordCreatePresenter presenter();
}
