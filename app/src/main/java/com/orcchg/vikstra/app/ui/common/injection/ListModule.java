package com.orcchg.vikstra.app.ui.common.injection;

import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;

import dagger.Module;

@Module
public class ListModule {

    protected final @BaseSelectAdapter.SelectMode int selectMode;

    public ListModule(@BaseSelectAdapter.SelectMode int selectMode) {
        this.selectMode = selectMode;
    }
}
