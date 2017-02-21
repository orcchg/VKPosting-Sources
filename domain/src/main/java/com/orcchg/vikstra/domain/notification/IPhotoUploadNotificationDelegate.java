package com.orcchg.vikstra.domain.notification;

public interface IPhotoUploadNotificationDelegate {
    void onPhotoUploadProgress(int progress, int total);
    void onPhotoUploadProgressInfinite();
    void onPhotoUploadStarted();
    void onPhotoUploadComplete();
}
