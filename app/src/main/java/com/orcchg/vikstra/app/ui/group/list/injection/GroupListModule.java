package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.ui.common.injection.KeywordModule;

import dagger.Module;

@Module
public class GroupListModule extends KeywordModule {

    public GroupListModule(long keywordBundleId) {
        super(keywordBundleId);
    }
}
