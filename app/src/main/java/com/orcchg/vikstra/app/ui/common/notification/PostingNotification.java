package com.orcchg.vikstra.app.ui.common.notification;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.report.main.ReportActivity;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

public class PostingNotification implements IPostingNotificationDelegate {

    private long groupReportBundleId, postId;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilderPosting;

    private String NOTIFICATION_POSTING_COMPLETE;

    public PostingNotification(Activity activity) {
        this(activity, Constant.BAD_ID, Constant.BAD_ID);
    }

    public PostingNotification(Activity activity, long groupReportBundleId, long postId) {
        this.groupReportBundleId = groupReportBundleId;
        this.postId = postId;

        Resources resources = activity.getResources();
        notificationManager = NotificationManagerCompat.from(activity);
        notificationBuilderPosting = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.drawable.ic_cloud_upload_white_18dp)
                .setContentTitle(resources.getString(R.string.notification_posting_title))
                .setContentText(resources.getString(R.string.notification_posting_description_progress))
                .setContentIntent(makePendingIntent(activity, groupReportBundleId, postId));

        NOTIFICATION_POSTING_COMPLETE = resources.getString(R.string.notification_posting_description_complete);
    }

    public void updateGroupReportBundleId(Activity activity, long groupReportBundleId) {
        notificationBuilderPosting.setContentIntent(makePendingIntent(activity, groupReportBundleId, postId));
    }

    public void updatePostId(Activity activity, long postId) {
        notificationBuilderPosting.setContentIntent(makePendingIntent(activity, groupReportBundleId, postId));
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
    private PendingIntent makePendingIntent(Activity activity, long groupReportBundleId, long postId) {
        Intent intent = ReportActivity.getCallingIntent(activity, groupReportBundleId, postId);
        return PendingIntent.getActivity(activity, GroupListActivity.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
