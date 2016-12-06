package com.orcchg.vikstra.app.ui.keyword.create.injection;

import com.orcchg.vikstra.app.ui.common.injection.KeywordModule;

import dagger.Module;

@Module
public class KeywordCreateModule extends KeywordModule {

    public KeywordCreateModule(long keywordBundleId) {
        super(keywordBundleId);
    }
}
