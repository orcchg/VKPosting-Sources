package com.orcchg.vikstra.app.ui.report;

import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class ReportFragment extends SimpleCollectionFragment implements ReportContract.SubView {
    public static final int RV_TAG = Constant.ListTag.REPORT_SCREEN;

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void showGroupReports(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }
}
