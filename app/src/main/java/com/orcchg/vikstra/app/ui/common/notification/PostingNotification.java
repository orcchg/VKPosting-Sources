package com.orcchg.vikstra.app.ui.common.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.report.main.ReportActivity;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

public class PostingNotification implements IPostingNotificationDelegate {

    private long groupReportBundleId, keywordBundleId, postId;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilderPosting;

    private String NOTIFICATION_POSTING_COMPLETE;

    public PostingNotification(Context context) {
        this(context, Constant.BAD_ID, Constant.BAD_ID, Constant.BAD_ID);
    }

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
                .setContentText(resources.getString(R.string.notification_posting_description_progress))
                .setContentIntent(makePendingIntent(context, groupReportBundleId, keywordBundleId, postId));

        NOTIFICATION_POSTING_COMPLETE = resources.getString(R.string.notification_posting_description_complete);
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

    @Override
    public void onPostingProgress(int progress, int total) {
        notificationBuilderPosting.setProgress(progress, total, false);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @Override
    public void onPostingProgressInfinite() {
        notificationBuilderPosting.setProgress(0, 0, true);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    @Override
    public void onPostingComplete() {
        notificationBuilderPosting.setContentText(NOTIFICATION_POSTING_COMPLETE).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.POSTING, notificationBuilderPosting.build());
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private PendingIntent makePendingIntent(Context context, long groupReportBundleId, long keywordBundleId, long postId) {
        Intent intent = ReportActivity.getCallingIntentNoInteractive(context, groupReportBundleId, keywordBundleId, postId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ReportActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(ReportActivity.REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
