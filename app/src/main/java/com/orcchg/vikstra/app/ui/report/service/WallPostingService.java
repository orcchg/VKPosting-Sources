package com.orcchg.vikstra.app.ui.report.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;

import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.ui.common.notification.PhotoUploadNotification;
import com.orcchg.vikstra.app.ui.common.notification.PostingNotification;
import com.orcchg.vikstra.app.ui.report.service.injection.DaggerWallPostingServiceComponent;
import com.orcchg.vikstra.app.ui.report.service.injection.WallPostingServiceComponent;
import com.orcchg.vikstra.app.ui.report.service.injection.WallPostingServiceModule;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class WallPostingService extends IntentService {
    public static final String NAME = "wall_posting_service";
    public static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    public static final String EXTRA_SELECTED_GROUPS = "extra_selected_groups";
    public static final String EXTRA_CURRENT_POST = "extra_current_post";
    public static final String OUT_EXTRA_WALL_POSTING_STATUS = "out_extra_wall_posting_status";

    public static final int WALL_POSTING_STATUS_STARTED = 0;
    public static final int WALL_POSTING_STATUS_FINISHED = 1;
    public static final int WALL_POSTING_STATUS_ERROR = 2;
    @IntDef({WALL_POSTING_STATUS_STARTED, WALL_POSTING_STATUS_FINISHED, WALL_POSTING_STATUS_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WallPostingStatus {}

    private long keywordBundleId = Constant.BAD_ID;
    private Post currentPost;

    private final Object lock = new Object();
    private boolean hasFinished = false;

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;

    private WallPostingServiceComponent component;

    public static Intent getCallingIntent(@NonNull Context context, long keywordBundleId,
                                          Collection<Group> selectedGroups, Post post) {
        ArrayList<Group> list = new ArrayList<>(selectedGroups);
        Intent intent = new Intent(context, WallPostingService.class);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBundleId);
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_GROUPS, list);
        intent.putExtra(EXTRA_CURRENT_POST, post);
        return intent;
    }

    public WallPostingService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("Enter Wall Posting service");
        component = DaggerWallPostingServiceComponent.builder()
                .applicationComponent(((AndroidApplication) getApplication()).getApplicationComponent())
                .wallPostingServiceModule(new WallPostingServiceModule())
                .build();

        ArrayList<Group> selectedGroups = intent.getParcelableArrayListExtra(EXTRA_SELECTED_GROUPS);
        keywordBundleId = intent.getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        currentPost = intent.getParcelableExtra(EXTRA_CURRENT_POST);

        initNotifications();  // cancel all previous notification and init the new ones

        sendPostingStartedMessage(WALL_POSTING_STATUS_STARTED);
        component.vkontakteEndpoint().makeWallPostsWithDelegate(selectedGroups, currentPost,
                createMakeWallPostCallback(), postingDelegate(), photoUploadDelegate());

        // wait for job's done
        synchronized (lock) {
            while (!hasFinished) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * This {@link IntentService} executes in separate background thread, but all callbacks of
         * it's use-cases are called from some {@link PostExecuteScheduler} corresponding to the
         * main (ui) thread. So, this scheduler will then notify service to continue and finish.
         */
        Timber.i("Exit Wall Posting service");
    }

    /* Notification delegate */
    // --------------------------------------------------------------------------------------------
    private void initNotifications() {
        NotificationManagerCompat.from(this).cancel(Constant.NotificationID.POSTING);
        NotificationManagerCompat.from(this).cancel(Constant.NotificationID.PHOTO_UPLOAD);

        postingNotification = new PostingNotification(this, Constant.BAD_ID, keywordBundleId, currentPost.id());
        photoUploadNotification = new PhotoUploadNotification(this);
    }

    @DebugLog
    private void sendPostingStartedMessage(@WallPostingStatus int status) {
        Timber.d("sendPostingStartedMessage: %s", status);
        if (status != WALL_POSTING_STATUS_STARTED) {
            synchronized (lock) {
                hasFinished = true;
                lock.notify();
            }
        }

        Intent intent = new Intent();
        intent.putExtra(OUT_EXTRA_WALL_POSTING_STATUS, status);
        sendBroadcast(intent);
    }

    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                Timber.i("Use-Case: succeeded to make wall posting");
                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(
                        reports, keywordBundleId, currentPost.id());
                PutGroupReportBundle useCase = component.putGroupReportBundleUseCase();
                useCase.setPostExecuteCallback(createPutGroupReportBundleCallback());
                useCase.setParameters(parameters);
                useCase.execute();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to make wall posting");
                sendPostingStartedMessage(WALL_POSTING_STATUS_ERROR);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupReportBundle> createPutGroupReportBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to put new GroupReportBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put GroupReportBundle");
                postingNotification.updateGroupReportBundleId(WallPostingService.this, bundle.id());
                sendPostingStartedMessage(WALL_POSTING_STATUS_FINISHED);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put GroupReportBundle");
                sendPostingStartedMessage(WALL_POSTING_STATUS_ERROR);
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private IPostingNotificationDelegate postingDelegate() {
        return new IPostingNotificationDelegate() {
            @Override
            public void onPostingProgress(int progress, int total) {
                postingNotification.onPostingProgress(progress, total);
            }

            @Override
            public void onPostingProgressInfinite() {
                postingNotification.onPostingProgressInfinite();
            }

            @Override
            public void onPostingComplete() {
                postingNotification.onPostingComplete();
            }
        };
    }

    private IPhotoUploadNotificationDelegate photoUploadDelegate() {
        return new IPhotoUploadNotificationDelegate() {
            @Override
            public void onPhotoUploadProgress(int progress, int total) {
                photoUploadNotification.onPhotoUploadProgress(progress, total);
            }

            @Override
            public void onPhotoUploadProgressInfinite() {
                photoUploadNotification.onPhotoUploadProgressInfinite();
            }

            @Override
            public void onPhotoUploadComplete() {
                photoUploadNotification.onPhotoUploadComplete();
            }
        };
    };
}
