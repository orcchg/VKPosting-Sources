package com.orcchg.vikstra.app.ui.common.notification;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

public class PhotoUploadNotification implements IPhotoUploadNotificationDelegate {

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilderPhotoUpload;

    private String NOTIFICATION_PHOTO_UPLOAD_COMPLETE;

    public PhotoUploadNotification(Context context) {
        Resources resources = context.getResources();
        notificationManager = NotificationManagerCompat.from(context);
        notificationBuilderPhotoUpload = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_collections_white_18dp)
                .setContentTitle(resources.getString(R.string.notification_photo_upload_title))
                .setContentText(resources.getString(R.string.notification_photo_upload_description_progress));

        NOTIFICATION_PHOTO_UPLOAD_COMPLETE = resources.getString(R.string.notification_photo_upload_description_complete);
    }

    @Override
    public void onPhotoUploadProgress(int progress, int total) {
        notificationBuilderPhotoUpload.setProgress(progress, total, false);
        notificationManager.notify(Constant.NotificationID.PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }

    @Override
    public void onPhotoUploadProgressInfinite() {
        notificationBuilderPhotoUpload.setProgress(0, 0, true);
        notificationManager.notify(Constant.NotificationID.PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }

    @Override
    public void onPhotoUploadComplete() {
        notificationBuilderPhotoUpload.setContentText(NOTIFICATION_PHOTO_UPLOAD_COMPLETE).setProgress(0, 0, false);
        notificationManager.notify(Constant.NotificationID.PHOTO_UPLOAD, notificationBuilderPhotoUpload.build());
    }
}
