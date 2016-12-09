package com.orcchg.vikstra.app.ui.main.injection;

import com.orcchg.vikstra.app.ui.keyword.list.KeywordListAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.injection.KeywordListModule;

import dagger.Module;

@Module
public class MainModule extends KeywordListModule {

    public MainModule(@KeywordListAdapter.SelectMode int selectMode) {
        super(selectMode);
    }
}
