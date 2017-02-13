package com.orcchg.vikstra.app.ui.report.history.viewholder;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.ReportHistoryListItemVO;

import butterknife.ButterKnife;

public class ReportHistoryViewHolder extends NormalViewHolder<ReportHistoryListItemVO> {

    public ReportHistoryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(ReportHistoryListItemVO reportHistoryListItemVO) {

    }
}
