package com.orcchg.vikstra.app.ui.report.history.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.view.KeywordsFlowLayout;
import com.orcchg.vikstra.app.ui.common.view.PostThumbnail;
import com.orcchg.vikstra.app.ui.viewobject.ReportHistoryListItemVO;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportHistoryViewHolder extends NormalViewHolder<ReportHistoryListItemVO> {
    private static String LABEL_STRING = null;

    @BindView(R.id.block_title) TextView titleView;
    @BindView(R.id.block_label) TextView labelView;
    @BindView(R.id.block_label_prefix) TextView labelPrefixView;
    @BindView(R.id.block_container) KeywordsFlowLayout flowLayout;
    @BindView(R.id.post_thumbnail) PostThumbnail postThumbnail;

    private BaseAdapter.OnItemClickListener<ReportHistoryListItemVO> postClickListener;

    public ReportHistoryViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        if (TextUtils.isEmpty(LABEL_STRING)) LABEL_STRING = itemView.getResources().getString(R.string.report_posted_counters);
    }

    public void setOnPostClickListener(BaseAdapter.OnItemClickListener<ReportHistoryListItemVO> postClickListener) {
        this.postClickListener = postClickListener;
    }

    @Override
    public void bind(ReportHistoryListItemVO viewObject) {
        String label = String.format(Locale.ENGLISH, LABEL_STRING, viewObject.posted(), viewObject.total());
        View.OnClickListener listener = createOnItemClickListener(viewObject);
        View.OnLongClickListener longListener = createOnItemLongClickListener(viewObject);

        titleView.setText(viewObject.dateTime());
        labelView.setText(label);
        flowLayout.setKeywords(viewObject.keywords());
        postThumbnail.setPost(viewObject.post());
        postThumbnail.setOnClickListener((view) -> {
            if (postClickListener != null) postClickListener.onItemClick(view, viewObject, getAdapterPosition());
        });

        itemView.setOnClickListener(listener);
        itemView.setOnLongClickListener(longListener);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private View.OnClickListener createOnItemClickListener(ReportHistoryListItemVO viewObject) {
        return (view) -> {
            if (listener != null) listener.onItemClick(view, viewObject, getAdapterPosition());
        };
    }

    private View.OnLongClickListener createOnItemLongClickListener(ReportHistoryListItemVO viewObject) {
        return (view) -> {
            if (longListener != null) longListener.onItemLongClick(view, viewObject, getAdapterPosition());
            return false;
        };
    }
}
