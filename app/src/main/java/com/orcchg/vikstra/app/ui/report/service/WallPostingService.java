package com.orcchg.vikstra.app.ui.report.service;

import android.app.IntentService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.ui.base.BaseIntentService;
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
import com.vk.sdk.VKServiceActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class WallPostingService extends BaseIntentService {
    private static final int FN_BASE_ID = 777;
    private static final int FOREGROUND_NOTIFICATION_ID = FN_BASE_ID;
    private static final String INTERNAL_EXTRA_START_SERVICE = "internal_extra_start_service";

    public static final String NAME = "wall_posting_service";
    public static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    public static final String EXTRA_SELECTED_GROUPS = "extra_selected_groups";
    public static final String EXTRA_CURRENT_POST = "extra_current_post";
    public static final String OUT_EXTRA_WALL_POSTING_PROGRESS = "out_extra_wall_posting_progress";
    public static final String OUT_EXTRA_WALL_POSTING_STATUS = "out_extra_wall_posting_status";
    public static final String OUT_EXTRA_WALL_POSTING_TOTAL = "out_extra_wall_posting_total";
    public static final String OUT_EXTRA_WALL_POSTING_RESULT_DATA_GROUP_REPORT_BUNDLE_ID = "out_extra_wall_posting_result_data_group_report_bundle_id";
    public static final String OUT_EXTRA_WALL_POSTING_RESULT_DATA_GROUP_REPORT_BUNDLE_TIMESTAMP = "out_extra_wall_posting_result_data_group_report_bundle_timestamp";

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
    private boolean hasPhotoUploadStarted = false;  // don't show notification, if photo uploading doesn't need

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;

    private WallPostingServiceComponent component;

    public static Intent getCallingIntent(@NonNull Context context, long keywordBundleId,
                                          Collection<Group> selectedGroups, Post post) {
        Timber.d("getCallingIntent: kw_id=%s, groups=%s, post=%s", keywordBundleId,
                (selectedGroups != null ? selectedGroups.size() : "null"),
                (post != null ? post.toString() : "null"));
        ArrayList<Group> list = new ArrayList<>(selectedGroups);
        Intent intent = new Intent(context, WallPostingService.class);
        intent.putExtra(INTERNAL_EXTRA_START_SERVICE, true);
        intent.putExtra(EXTRA_KEYWORD_BUNDLE_ID, keywordBundleId);
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_GROUPS, list);
        intent.putExtra(EXTRA_CURRENT_POST, post);
        return intent;
    }

    public WallPostingService() {
        super(NAME);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("Service onCreate");
        component = DaggerWallPostingServiceComponent.builder()
                .applicationComponent(((AndroidApplication) getApplication()).getApplicationComponent())
                .wallPostingServiceModule(new WallPostingServiceModule())
                .build();

        IntentFilter filterCaptcha = new IntentFilter(VKServiceActivity.VK_SERVICE_BROADCAST);
        IntentFilter filterInterrupt = new IntentFilter(Constant.Broadcast.WALL_POSTING_INTERRUPT);
        IntentFilter filterSuspend = new IntentFilter(Constant.Broadcast.WALL_POSTING_SUSPEND);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverCaptcha, filterCaptcha);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverInterrupt, filterInterrupt);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverSuspend, filterSuspend);
    }

    @Override
    public void onDestroy() {
        Timber.i("Service onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCaptcha);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverInterrupt);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverSuspend);
        super.onDestroy();
    }

    /* Payload */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        Timber.i("Enter Wall Posting service");
        boolean valid = intent.getBooleanExtra(INTERNAL_EXTRA_START_SERVICE, false);
        if (!valid) {
            Timber.w("Received intent which is not intended for Wall Posting service. Finish...");
            return;
        }

        ArrayList<Group> selectedGroups = intent.getParcelableArrayListExtra(EXTRA_SELECTED_GROUPS);
        keywordBundleId = intent.getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        currentPost = intent.getParcelableExtra(EXTRA_CURRENT_POST);

        initNotifications();  // cancel all previous notification and init the new ones

        becomeForeground();

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

    /* Broadcast receiver */
    // --------------------------------------------------------------------------------------------
    private BroadcastReceiver receiverCaptcha = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * Resume wall posting is the process has just recovered from Captcha error successfully.
             */
            Timber.d("Received Captcha recovered signal");
            int outerCode = intent.getIntExtra(VKServiceActivity.VK_SERVICE_OUT_KEY_TYPE, -1);
            boolean captchaRecovered = outerCode == VKServiceActivity.VKServiceType.Captcha.getOuterCode();
            Timber.d("Captcha(%s): %s", outerCode, captchaRecovered);
            onWallPostingSuspend(!captchaRecovered);
        }
    };

    private BroadcastReceiver receiverInterrupt = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received interrupt signal");
            onWallPostingInterrupt();
        }
    };

    private BroadcastReceiver receiverSuspend = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * Pause / resume wall posting when user has explicitly asked for pause / resume ('paused' == true / false).
             */
            Timber.d("Received suspend signal");
            boolean paused = intent.getBooleanExtra(Constant.Broadcast.WALL_POSTING_SUSPEND, false);
            Timber.d("Explicit pause: %s", paused);
            onWallPostingSuspend(paused);
        }
    };

    /* Notification delegate */
    // --------------------------------------------------------------------------------------------
    private void becomeForeground() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle(getResources().getString(R.string.notification_posting_title))
                .setContentText(getResources().getString(R.string.notification_posting_description_progress));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(this, R.drawable.ic_app));
        }
        Notification notification = builder.build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

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

        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_STATUS);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_STATUS, status);
        sendBroadcast(intent);
    }

    void sendPostingProgress(int progress, int total) {
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_PROGRESS);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_PROGRESS, progress);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_TOTAL, total);
        sendBroadcast(intent);
    }

    void sendNewGroupReportBundleEssentials(long groupReportBundleId, long timestamp) {
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_RESULT_DATA);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_RESULT_DATA_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_RESULT_DATA_GROUP_REPORT_BUNDLE_TIMESTAMP, timestamp);
        sendBroadcast(intent);
    }

    // ------------------------------------------
    @DebugLog
    private void onWallPostingInterrupt() {
        Timber.i("onWallPostingInterrupt: startHandle=%s", wasStartedHandle());
        // check for null in case initial Intent is for receiver, not for 'onHandleIntent'
        if (postingNotification != null) postingNotification.onPostingInterrupt();
        if (hasPhotoUploadStarted && photoUploadNotification != null) photoUploadNotification.onPhotoUploadInterrupt();

        synchronized (lock) {
            hasFinished = true;
            lock.notify();
        }
        Timber.d("Finishing service after wall posting interruption...");
    }

    @DebugLog
    private void onWallPostingSuspend(boolean paused) {
        Timber.i("onWallPostingSuspend: paused=%s, startHandle=%s, component=%s",
                paused, wasStartedHandle(), (component != null ? component.hashCode() : "null"));
        if (paused) {
            component.vkontakteEndpoint().pauseWallPosting();
        } else {
            component.vkontakteEndpoint().resumeWallPosting();
        }
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
                sendNewGroupReportBundleEssentials(bundle.id(), bundle.timestamp());
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
            public void onPostingStarted() {
                // no-op
            }

            @DebugLog @Override
            public void onPostingProgress(int progress, int total) {
                if (progress == Constant.INIT_PROGRESS && total == Constant.INIT_PROGRESS) return;
                postingNotification.onPostingProgress(progress, total);
                sendPostingProgress(progress, total);
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
            public void onPhotoUploaStarted() {
                hasPhotoUploadStarted = true;
            }

            @DebugLog @Override
            public void onPhotoUploadProgress(int progress, int total) {
                if (progress == Constant.INIT_PROGRESS && total == Constant.INIT_PROGRESS) return;
                photoUploadNotification.onPhotoUploadProgress(progress, total);
            }

            @Override
            public void onPhotoUploadProgressInfinite() {
                if (hasPhotoUploadStarted) photoUploadNotification.onPhotoUploadProgressInfinite();
            }

            @Override
            public void onPhotoUploadComplete() {
                if (hasPhotoUploadStarted) photoUploadNotification.onPhotoUploadComplete();
            }
        };
    };
}
