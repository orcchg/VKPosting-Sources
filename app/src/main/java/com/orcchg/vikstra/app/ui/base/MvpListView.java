package com.orcchg.vikstra.app.ui.base;

import android.support.v7.widget.RecyclerView;

public interface MvpListView extends MvpView {
    RecyclerView getListView(int tag);
}
