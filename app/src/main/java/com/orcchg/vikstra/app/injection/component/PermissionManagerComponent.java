package com.orcchg.vikstra.app.injection.component;

import com.orcchg.vikstra.app.PermissionManager;
import com.orcchg.vikstra.app.injection.module.PermissionManagerModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {PermissionManagerModule.class})
public interface PermissionManagerComponent {

    PermissionManager permissionManager();
}
