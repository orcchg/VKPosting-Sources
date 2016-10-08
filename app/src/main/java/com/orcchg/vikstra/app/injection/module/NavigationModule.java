package com.orcchg.vikstra.app.injection.module;

import com.orcchg.vikstra.app.navigation.Navigator;

import dagger.Module;
import dagger.Provides;

@Module
public class NavigationModule {

    @Provides
    Navigator provideNavigator() {
        return new Navigator();
    }
}
