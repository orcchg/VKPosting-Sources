package com.orcchg.vikstra.app.ui.group.custom.activity;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;

import javax.inject.Inject;

public class GroupCustomListPresenter extends BaseListPresenter<GroupCustomListContract.View>
        implements GroupCustomListContract.Presenter {

    @Inject
    GroupCustomListPresenter() {
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
    public void onBackPressed() {

    }

    @Override
    public void onFabClick() {

    }

    @Override
    public void onPostThumbnailClick(long postId) {

    }

    @Override
    public void onTitleChanged(String text) {

    }

    @Override
    public void retry() {

    }

    @Override
    public void retryPost() {

    }

    @Override
    public void setPostingTimeout(int timeout) {

    }

    @Override
    protected void freshStart() {

    }

    @Override
    protected void onRestoreState() {

    }
}
