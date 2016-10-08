package com.orcchg.vikstra.app.injection.component;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.module.NavigationModule;
import com.orcchg.vikstra.app.navigation.Navigator;
import com.orcchg.vikstra.app.ui.list.ListActivity;

import dagger.Component;

@PerActivity
@Component(modules = {NavigationModule.class})
public interface NavigationComponent {

    void inject(ListActivity activity);

    Navigator navigator();
}
