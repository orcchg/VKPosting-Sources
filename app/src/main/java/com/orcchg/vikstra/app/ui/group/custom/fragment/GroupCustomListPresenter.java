package com.orcchg.vikstra.app.ui.group.custom.fragment;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;

import javax.inject.Inject;

public class GroupCustomListPresenter extends BaseListPresenter<GroupCustomListContract.View>
        implements GroupCustomListContract.Presenter {

    @Inject
    GroupCustomListPresenter() {
    }

    @Override
    public void refresh() {

    }

    @Override
    public void retry() {

    }

    @Override
    protected BaseAdapter createListAdapter() {
        return null;
    }

    @Override
    protected int getListTag() {
        return 0;
    }

    @Override
    protected void onLoadMore() {

    }

    @Override
    protected void freshStart() {

    }

    @Override
    protected void onRestoreState() {

    }
}
