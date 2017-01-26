package com.orcchg.vikstra.app.ui.report.viewholder;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.domain.model.GroupReport;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportViewHolder extends NormalViewHolder<ReportListItemVO> {

    @BindView(R.id.tv_title) TextView titleTextView;
    @BindView(R.id.tv_count) TextView countTextView;
    @BindView(R.id.iv_status) ImageView statusView;

    private static @ColorInt int sCancelledColor = -1;
    private static @ColorInt int sFailureColor = -1;
    private static @ColorInt int sSuccessColor = -1;

    public ReportViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);

        Context context = itemView.getContext();
        if (sCancelledColor == -1) sCancelledColor = ContextCompat.getColor(context, R.color.report_screen_status_cancelled);
        if (sFailureColor == -1) sFailureColor = ContextCompat.getColor(context, R.color.report_screen_status_failure);
        if (sSuccessColor == -1) sSuccessColor = ContextCompat.getColor(context, R.color.report_screen_status_success);
    }

    @Override
    public void bind(ReportListItemVO viewObject) {
        titleTextView.setText(viewObject.groupName());
        countTextView.setText(String.format(Locale.ENGLISH, "%s", viewObject.membersCount()));

        @GroupReport.Status int status = viewObject.reportStatus();
        switch (status) {
            case GroupReport.STATUS_CANCEL:
                statusView.setImageResource(R.drawable.ic_priority_high_white_24dp);
                statusView.setColorFilter(sCancelledColor);
                break;
            case GroupReport.STATUS_SUCCESS:
                statusView.setImageResource(R.drawable.ic_check_white_24dp);
                statusView.setColorFilter(sSuccessColor);
                break;
            case GroupReport.STATUS_FAILURE:
                statusView.setImageResource(R.drawable.ic_clear_white_24dp);
                statusView.setColorFilter(sFailureColor);
                break;
        }
    }
}
