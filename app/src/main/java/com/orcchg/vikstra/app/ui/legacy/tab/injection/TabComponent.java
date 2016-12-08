package com.orcchg.vikstra.app.ui.legacy.tab.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.legacy.tab.TabActivity;
import com.orcchg.vikstra.app.ui.legacy.tab.TabPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {TabModule.class}, dependencies = {ApplicationComponent.class})
public interface TabComponent {

    void inject(TabActivity activity);

    TabPresenter presenter();
}
