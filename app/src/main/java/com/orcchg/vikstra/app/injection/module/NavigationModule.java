package com.orcchg.vikstra.app.injection.module;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.navigation.Navigator;

import dagger.Module;
import dagger.Provides;

@Module
public class NavigationModule {

    @Provides @PerActivity
    Navigator provideNavigator() {
        return new Navigator();
    }
}
