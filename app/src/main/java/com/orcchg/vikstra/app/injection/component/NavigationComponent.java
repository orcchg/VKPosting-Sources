package com.orcchg.vikstra.app.injection.component;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.module.NavigationModule;
import com.orcchg.vikstra.app.navigation.Navigator;
import com.orcchg.vikstra.app.navigation.NavigatorHolder;

import dagger.Component;

@PerActivity
@Component(modules = {NavigationModule.class})
public interface NavigationComponent {

    void inject(NavigatorHolder holder);

    Navigator navigator();
}
