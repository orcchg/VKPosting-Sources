package com.orcchg.vikstra.data.source.remote.injection;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {CloudModule.class})
public interface CloudComponent {
}
