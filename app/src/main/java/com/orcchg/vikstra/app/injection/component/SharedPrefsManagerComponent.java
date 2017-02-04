package com.orcchg.vikstra.app.injection.component;

import com.orcchg.vikstra.app.SharedPrefsManager;
import com.orcchg.vikstra.app.injection.module.SharedPrefsManagerModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {SharedPrefsManagerModule.class})
public interface SharedPrefsManagerComponent {

    SharedPrefsManager sharedPrefsManager();
}
