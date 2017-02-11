package com.orcchg.vikstra.app.ui.report.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.report.main.viewholder.ReportViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;

public class ReportAdapter extends BaseAdapter<ReportViewHolder, ReportListItemVO> {

    @Override
    protected ReportViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_group_report, parent, false);
        ReportViewHolder viewHolder = new ReportViewHolder(view);
        viewHolder.setOnItemClickListener(onItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        return viewHolder;
    }

    public void setItemReverted(int position, boolean reverted) {
        setItemRevertedSilent(position, reverted);
        notifyItemChanged(position);
    }

    public void setItemRevertedSilent(int position, boolean reverted) {
        ReportListItemVO vo = models.get(position);
        vo.setReverted(reverted);
    }

    public void setAllItemsReverted(boolean reverted) {
        for (ReportListItemVO vo : models) {
            vo.setReverted(reverted);
        }
        notifyDataSetChanged();
    }
}
