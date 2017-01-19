package com.orcchg.vikstra.data.injection.remote;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {CloudModule.class})
public interface CloudComponent {
}
