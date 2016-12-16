package com.orcchg.vikstra.app.ui.common.injection;

import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;

import dagger.Module;

@Module
public class ListModule {

    protected final @BaseAdapter.SelectMode int selectMode;

    public ListModule(@BaseAdapter.SelectMode int selectMode) {
        this.selectMode = selectMode;
    }
}
