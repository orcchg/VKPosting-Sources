package com.orcchg.vikstra.app.ui.report.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.report.history.viewholder.ReportHistoryViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.ReportHistoryListItemVO;

public class ReportHistoryAdapter extends BaseAdapter<ReportHistoryViewHolder, ReportHistoryListItemVO> {

    private BaseAdapter.OnItemClickListener<ReportHistoryListItemVO> postClickListener;

    public void setOnPostClickListener(BaseAdapter.OnItemClickListener<ReportHistoryListItemVO> postClickListener) {
        this.postClickListener = postClickListener;
    }

    @Override
    protected ReportHistoryViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_report_history, parent, false);
        ReportHistoryViewHolder viewHolder = new ReportHistoryViewHolder(view);
        viewHolder.setOnItemClickListener(onItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        viewHolder.setOnPostClickListener(postClickListener);
        return viewHolder;
    }
}
