package com.orcchg.vikstra.app.injection.component;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.module.NavigationModule;
import com.orcchg.vikstra.app.navigation.Navigator;
import com.orcchg.vikstra.app.navigation.NavigatorHolder;
import com.orcchg.vikstra.app.ui.legacy.list.ListActivity;

import dagger.Component;

@PerActivity
@Component(modules = {NavigationModule.class})
public interface NavigationComponent {

    void inject(NavigatorHolder holder);
    void inject(ListActivity activity);  // TODO(compat): remove with ListActivity

    Navigator navigator();
}
