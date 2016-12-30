package com.orcchg.vikstra.app.ui.status;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.stub.SimpleBaseDialogFragment;
import com.orcchg.vikstra.domain.util.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StatusDialogFragment extends SimpleBaseDialogFragment implements StatusContract.View {
   public static final String DIALOG_TAG = "status_dialog_tag";

    @BindView(R.id.circle_loading_view) AnimatedCircleLoadingView progressView;
    @BindView(R.id.btn_report) Button reportButton;
    @OnClick(R.id.btn_report)
    void onReportClick() {
        openReportScreen();
    }

    private long groupReportBundleId = Constant.BAD_ID;
    private long postId = Constant.BAD_ID;

    public static StatusDialogFragment newInstance() {
        return new StatusDialogFragment();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = inflater.inflate(R.layout.dialog_fragment_status, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        progressView.startIndeterminate();
//        progressView.startDeterminate();  // TODO: already has a parent
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // TODO: set background label
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void openReportScreen() {
        navigationComponent.navigator().openReportScreen(getActivity(), groupReportBundleId, postId);
        dismiss();  // TODO: don't set background label
    }

    /* API */
    // ------------------------------------------
    public void onPostingComplete() {
        progressView.post(() -> progressView.stopOk());
    }

    public void onReportReady(long groupReportBundleId, long postId) {
        this.groupReportBundleId = groupReportBundleId;
        this.postId = postId;
        reportButton.post(() -> reportButton.setVisibility(View.VISIBLE));
    }

    public void updatePostingProgress(int progress, int total) {
        int percent = progress * 100 / total;
        progressView.post(() -> progressView.setPercent(percent));
    }
}
