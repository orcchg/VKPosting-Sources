package com.orcchg.vikstra.data.injection.migration;

import javax.inject.Singleton;

import dagger.Component;
import io.realm.RealmConfiguration;

@Singleton
@Component(modules = {MigrationModule.class})
public interface MigrationComponent {

    RealmConfiguration realmConfiguration();
}
