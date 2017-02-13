package com.orcchg.vikstra.app.ui.report.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class ReportHistoryPresenter extends BaseListPresenter<ReportHistoryContract.View>
        implements ReportHistoryContract.Presenter {

    private Memento memento = new Memento();

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        //
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    ReportHistoryPresenter() {
    }

    @Override
    protected BaseAdapter createListAdapter() {
        return null;
    }

    @Override
    protected int getListTag() {
        return 0;
    }

    /* State */
    // --------------------------------------------------------------------------------------------
    /**
     * State machine:
     *
     *                           START
     */

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------

    /* List */
    // ------------------------------------------
    @Override
    protected void onLoadMore() {
        // TODO: on load more
    }

    private void retryLoadMore() {
        listAdapter.onError(false); // show loading more
        // TODO: load more limit-offset
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {

    }

    @Override
    protected void onRestoreState() {

    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
}
