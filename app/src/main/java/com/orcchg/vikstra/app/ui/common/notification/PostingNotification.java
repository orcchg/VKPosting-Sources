package com.orcchg.vikstra.app.ui.common.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.report.main.ReportActivity;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

import hugo.weaving.DebugLog;

public class PostingNotification implements IPostingNotificationDelegate {

    private long groupReportBundleId, keywordBundleId, postId;
    private boolean hasPostingFinished = false;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilderPosting;

    private String NOTIFICATION_POSTING_COMPLETE, NOTIFICATION_POSTING_INTERRUPT,
            NOTIFICATION_POSTING_PAUSED, NOTIFICATION_POSTING_PROGRESS;

    public PostingNotification(Context context, long groupReportBundleId, long keywordBundleId, long postId) {
        this.groupReportBundleId = groupReportBundleId;
        this.keywordBundleId = keywordBundleId;
        this.postId = postId;

        Resources resources = context.getResources();
        notificationManager = NotificationManagerCompat.from(context);
        notificationBuilderPosting = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle(resources.getString(R.string.notification_posting_title))
                .setContentText(NOTIFICATION_POSTING_PROGRESS)
                .setContentIntent(makePendingIntent(context, groupReportBundleId, keywordBundleId, postId));

        NOTIFICATION_POSTING_COMPLETE = resources.getString(R.string.notification_posting_description_complete);
        NOTIFICATION_POSTING_INTERRUPT = resources.getString(R.string.notification_posting_description_interrupt);
        NOTIFICATION_POSTING_PAUSED = resources.getString(R.string.notification_posting_description_pause);
        NOTIFICATION_POSTING_PROGRESS = resources.getString(R.string.notification_posting_description_progress);
    }

    public void updateGroupReportBundleId(Context context, long groupReportBundleId) {
        notificationBuilderPosting.setContentIntent(makePendingIntent(context, groupReportBundleId, keywordBundleId, postId));
    }

    public void updateKeywordBundleId(Context context, long keywordBundleId) {
        notificationBuilderPosting.setContentIntent(makePendingIntent(context, groupReportBundleId, keywordBundleId, postId));
    }

    public void updatePostId(Context context, long postId) {
        notificationBuilderPosting.setContentIntent(makePendingIntent(context, groupReportBundleId, keywordBundleId, postId));
    }

    @DebugLog @Override
    public void onPostingProgress(int progress, int total) {
        hasPostingFinished = false;
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_PROGRESS).setProgress(total, progress, false);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @DebugLog @Override
    public void onPostingProgressInfinite() {
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_PROGRESS).setProgress(0, 0, true);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @DebugLog @Override
    public void onPostingStarted() {
        // already initialized in ctor
    }

    @DebugLog @Override
    public void onPostingComplete() {
        hasPostingFinished = true;
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_COMPLETE).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @DebugLog
    public void onPostingPaused() {
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_PAUSED);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @DebugLog
    public void onPostingInterrupt() {
        hasPostingFinished = true;
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_INTERRUPT).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private PendingIntent makePendingIntent(Context context, long groupReportBundleId, long keywordBundleId, long postId) {
        Intent intent;
        if (hasPostingFinished) {
            intent = ReportActivity.getCallingIntentNoInteractive(context, groupReportBundleId, keywordBundleId, postId);
        } else {  // 'groupReportBundleId' could be BAD_ID here, so don't open ReportScreen in non-interactive mode
            intent = ReportActivity.getCallingIntent(context, groupReportBundleId, keywordBundleId, postId);
        }
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(ReportActivity.class);
//        stackBuilder.addNextIntent(intent);
//        return stackBuilder.getPendingIntent(ReportActivity.REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
        return PendingIntent.getActivity(context, ReportActivity.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
