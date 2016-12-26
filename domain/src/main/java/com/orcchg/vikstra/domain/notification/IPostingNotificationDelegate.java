package com.orcchg.vikstra.domain.notification;

public interface IPostingNotificationDelegate {
    void onPostingProgress(int progress, int total);
    void onPostingProgressInfinite();
    void onPostingComplete();
}
