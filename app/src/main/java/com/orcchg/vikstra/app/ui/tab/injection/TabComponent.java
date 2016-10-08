package com.orcchg.vikstra.app.ui.tab.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.tab.TabActivity;
import com.orcchg.vikstra.app.ui.tab.TabPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {TabModule.class}, dependencies = {ApplicationComponent.class})
public interface TabComponent {

    void inject(TabActivity activity);

    TabPresenter presenter();
}
