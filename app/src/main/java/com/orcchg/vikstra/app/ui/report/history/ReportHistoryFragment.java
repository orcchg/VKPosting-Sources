package com.orcchg.vikstra.app.ui.report.history;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.app.ui.common.screen.CollectionFragment;
import com.orcchg.vikstra.app.ui.report.history.injection.DaggerReportHistoryComponent;
import com.orcchg.vikstra.app.ui.report.history.injection.ReportHistoryComponent;
import com.orcchg.vikstra.app.ui.report.history.injection.ReportHistoryModule;
import com.orcchg.vikstra.domain.util.Constant;

public class ReportHistoryFragment extends CollectionFragment<ReportHistoryContract.View, ReportHistoryContract.Presenter>
        implements ReportHistoryContract.View {
    public static final int RV_TAG = Constant.ListTag.REPORT_HISTORY_SCREEN;

    private ReportHistoryComponent reportHistoryComponent;

    @NonNull @Override
    protected ReportHistoryContract.Presenter createPresenter() {
        return reportHistoryComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        reportHistoryComponent = DaggerReportHistoryComponent.builder()
                .applicationComponent(getApplicationComponent())
                .reportHistoryModule(new ReportHistoryModule())  // TODO: pass ids
                .build();
        reportHistoryComponent.inject(this);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    public static ReportHistoryFragment newInstance() {
        Bundle args = new Bundle();
        // TODO: args
        ReportHistoryFragment fragment = new ReportHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();// TODO: args
        super.onCreate(savedInstanceState);
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        // TODO: impl view
        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    // TODO: impl
}
