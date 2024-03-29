package com.orcchg.vikstra.app.ui.report.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
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
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api220VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPost;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.misc.PostingUnit;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.vk.sdk.VKServiceActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    public static final String OUT_EXTRA_WALL_POSTING_PROGRESS_UNIT = "out_extra_wall_posting_progress_unit";
    public static final String OUT_EXTRA_WALL_POSTING_CANCEL_REASON_CODE = "out_extra_wall_posting_cancel_reason_class_name";
    public static final String OUT_EXTRA_WALL_POSTING_GROUP_REPORT_BUNDLE_ID = "out_extra_wall_posting_group_report_bundle_id";

    public static final int WALL_POSTING_STATUS_STARTED = 0;
    public static final int WALL_POSTING_STATUS_FINISHED = 1;
    public static final int WALL_POSTING_STATUS_ERROR = 2;
    @IntDef({WALL_POSTING_STATUS_STARTED, WALL_POSTING_STATUS_FINISHED, WALL_POSTING_STATUS_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WallPostingStatus {}

    private Queue<Task> tasks = new LinkedList<>();

    private long keywordBundleId = Constant.BAD_ID;
    private Post currentPost;

    private final Object lock = new Object();
    private volatile boolean hasFinished = false;
    private boolean hasPhotoUploadStarted = false;  // don't show notification, if photo uploading doesn't need
    private boolean shouldUploadMedia = false;
    private boolean wasPaused = false;
    private boolean wasCancelled = false;
    private Throwable cancelReason;

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;
    private Notification.Builder serviceNotificationBuilder;

    private WallPostingServiceComponent component;

    int postedWithCancel = 0;
    int postedWithFailure = 0;
    int postedWithSuccess = 0;
    int totalForPosting = 0;

    private List<GroupReportEssence> storedReports = new ArrayList<>();

    // --------------------------------------------------------------------------------------------
    private static final int TASK_INTERRUPT = 0;
    private static final int TASK_SCREEN_DESTROY = 1;
    private static final int TASK_SUSPEND_PAUSE = 2;
    private static final int TASK_SUSPEND_RESUME = 3;
    @IntDef({
        TASK_INTERRUPT,
        TASK_SCREEN_DESTROY,
        TASK_SUSPEND_PAUSE,
        TASK_SUSPEND_RESUME
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface TaskId {}

    private static final class Task {
        private final @TaskId int id;
        private final Runnable runnable;

        private Task(@TaskId int id, Runnable runnable) {
            this.id = id;
            this.runnable = runnable;
        }
    }

    // --------------------------------------------------------------------------------------------
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

        becomeForeground();

        IntentFilter filterCaptcha = new IntentFilter(VKServiceActivity.VK_SERVICE_BROADCAST);
        IntentFilter filterInterrupt = new IntentFilter(Constant.Broadcast.WALL_POSTING_INTERRUPT);
        IntentFilter filterScreenDestroy = new IntentFilter(Constant.Broadcast.WALL_POSTING_SCREEN_DESTROY);
        IntentFilter filterSuspend = new IntentFilter(Constant.Broadcast.WALL_POSTING_SUSPEND);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverCaptcha, filterCaptcha);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverInterrupt, filterInterrupt);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverScreenDestroy, filterScreenDestroy);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverSuspend, filterSuspend);
    }

    @Override
    public void onDestroy() {
        Timber.i("Service onDestroy");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverCaptcha);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverInterrupt);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverScreenDestroy);
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

        notifyPostingStatus(WALL_POSTING_STATUS_STARTED);
        shouldUploadMedia = component.vkontakteEndpoint().makeWallPostsWithDelegate(selectedGroups, currentPost,
                createMakeWallPostCallback(), createProgressCallback(),
                createPostingCancelledCallback(), postingDelegate(), photoUploadDelegate());

        // wait for job's done
        synchronized (lock) {
            while (!hasFinished) {
                try {
                    lock.wait();
                    Timber.v("Service idling... [hasFinished=%s], tasks=%s", hasFinished, tasks.size());
                    pollTasksAndRun();
                } catch (InterruptedException e) {
                    Timber.w("Interrupted exception on Service");
                    Thread.currentThread().interrupt();
                    pollTasksAndRun();  // ensure all tasks are performed
                }
            }
        }

        /**
         * This {@link IntentService} executes in separate background thread, but all callbacks of
         * it's use-cases are called from some {@link PostExecuteScheduler} corresponding to the
         * main (ui) thread. So, this scheduler will then notify service to continue and finish.
         */
        Timber.i("Exit Wall Posting service");
        stopForeground(true);
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

    private BroadcastReceiver receiverScreenDestroy = new BroadcastReceiver() {
        @DebugLog @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received screen destroy signal");
            onScreenDestroyed();
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
        serviceNotificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_app_notification)
                .setContentTitle(getResources().getString(R.string.notification_posting_title))
                .setContentText(getResources().getString(R.string.notification_posting_description_progress))
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            serviceNotificationBuilder.setLargeIcon(Icon.createWithResource(this, R.drawable.ic_app));
        }
        Notification notification = serviceNotificationBuilder.build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    private void initNotifications() {
        PendingIntent intent = PostingNotification.makePendingIntent(this, hasFinished,
                Constant.BAD_ID, keywordBundleId, currentPost.id());
        serviceNotificationBuilder.setContentIntent(intent);
        Notification notification = serviceNotificationBuilder.build();
        NotificationManagerCompat.from(this).notify(FOREGROUND_NOTIFICATION_ID, notification);

        dropPostingNotification();
        dropPhotoUploadNotification();
        postingNotification = new PostingNotification(this, Constant.BAD_ID, keywordBundleId, currentPost.id());
        photoUploadNotification = new PhotoUploadNotification(this);
    }

    /* Broadcasts */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void notifyPostingStatus(@WallPostingStatus int status) {
        Timber.d("notifyPostingStatus: %s", status);
        if (status != WALL_POSTING_STATUS_STARTED) leave();
    }

    // ------------------------------------------
    @DebugLog
    void sendPostingResult(PostingUnit postingUnit) {
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_PROGRESS_UNIT);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_PROGRESS_UNIT, postingUnit);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @DebugLog
    void sendPostingFinished(long groupReportBundleId) {
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_FINISHED);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @DebugLog
    void sendPostingCancelled(Throwable reason, long groupReportBundleId) {
        int apiErrorCode = EndpointUtility.errorCode(reason);
        Intent intent = new Intent(Constant.Broadcast.WALL_POSTING_CANCELLED);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_CANCEL_REASON_CODE, apiErrorCode);
        intent.putExtra(OUT_EXTRA_WALL_POSTING_GROUP_REPORT_BUNDLE_ID, groupReportBundleId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // --------------------------------------------------------------------------------------------
    @DebugLog
    private void onScreenDestroyed() {
        Timber.i("onScreenDestroyed, paused=%s", wasPaused);
        performScreenDestroyed();
    }

    @DebugLog
    private void onWallPostingInterrupt() {
        Timber.i("onWallPostingInterrupt");
        performInterruption();
    }

    @DebugLog
    private void onWallPostingSuspend(boolean paused) {
        Timber.i("onWallPostingSuspend: paused=%s, component=%s",
                paused, (component != null ? component.hashCode() : "null"));
        performSuspension(paused);
    }

    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                if (reports == null) {
                    Timber.e("Make wall posting has finished with null data - it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to make wall posting");
                persistReports(reports, createPutGroupReportBundleCallback());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to make wall posting");
                notifyPostingStatus(WALL_POSTING_STATUS_ERROR);
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
                if (wasCancelled) {
                    sendPostingCancelled(cancelReason, bundle.id());
                } else {
                    sendPostingFinished(bundle.id());
                }
                updateGroupReportBundleIdForNotifications(bundle.id());
                notifyPostingStatus(WALL_POSTING_STATUS_FINISHED);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put GroupReportBundle");
                notifyPostingStatus(WALL_POSTING_STATUS_ERROR);
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private MultiUseCase.CancelCallback createPostingCancelledCallback() {
        return (reason) -> {
            wasCancelled = true;
            cancelReason = reason;
            /**
             * Cancel means the same as interruption: both will set
             * {@link PostingNotification#hasPostingFinished} flag properly and then we fallback to
             * {@link WallPostingService#createPutGroupReportBundleCallback()}'s onFinish() method,
             * where we update GroupReportBundle id in notification.
             *
             * If both flag and id are properly set, notification will open ReportScreen
             * in non-interactive mode with the most actual data.
             */
            if (postingNotification != null) postingNotification.onPostingInterrupt();
            if (hasPhotoUploadStarted && photoUploadNotification != null) photoUploadNotification.onPhotoUploadInterrupt();
        };
    }

    private MultiUseCase.ProgressCallback<GroupReportEssence> createProgressCallback() {
        return (index, total, item) -> {
            if (index == Constant.INIT_PROGRESS && total == Constant.INIT_PROGRESS) return;  // skip start event

            Timber.v("Posting progress: %s / %s", index + 1, total);
            totalForPosting = total;
            if (item.data != null)  ++postedWithSuccess;  // count successful posting
            if (item.error != null) ++postedWithFailure;  // count failed posting
            /**
             * Flag {@link Ordered#cancelled} is not checked here because it could be true and
             * {@link Ordered#data} or {@link Ordered#error} could not be null at the same time.
             */
            if (item.data == null && item.error == null) ++postedWithCancel;  // count cancelled posting

            // prepare model from data
            MakeWallPost.Parameters params = (MakeWallPost.Parameters) item.parameters;
            Group group = params.getGroup();  // null parameters are impossible because this is checked inside the use-case
            Timber.v("%s", group.toString());
            // TODO: use terminal error from proper UseCase instead of hardcoded one
            GroupReportEssence essence = VkontakteEndpoint.refineModel(item, group, Api5VkUseCaseException.class, Api220VkUseCaseException.class);
            storedReports.add(essence);

            long timestamp = System.currentTimeMillis();
            component.groupReportEssenceMapper().setGroupReportId(Constant.INIT_ID);  // fictive id
            component.groupReportEssenceMapper().setTimestamp(timestamp);
            GroupReport model = component.groupReportEssenceMapper().map(essence);

            PostingUnit unit = PostingUnit.builder()
                    .setCancelCount(postedWithCancel)
                    .setFailureCount(postedWithFailure)
                    .setSuccessCount(postedWithSuccess)
                    .setTotalCount(total)
                    .setGroupReport(model)
                    .build();

            sendPostingResult(unit);
        };
    }

    // ------------------------------------------
    private IPostingNotificationDelegate postingDelegate() {
        return new IPostingNotificationDelegate() {
            @Override
            public void onPostingStarted() {
                // no-op
            }

            @DebugLog @Override
            public void onPostingProgress(int progress, int total) {
                if (wasPaused ||  // ignore tardy progress unit after pause and starting callback
                    progress == Constant.INIT_PROGRESS && total == Constant.INIT_PROGRESS) {
                    return;
                }
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
            public void onPhotoUploadStarted() {
                hasPhotoUploadStarted = true;
            }

            @DebugLog @Override
            public void onPhotoUploadProgress(int progress, int total) {
                if (wasPaused ||  // ignore tardy progress unit after pause and starting callback
                    progress == Constant.INIT_PROGRESS && total == Constant.INIT_PROGRESS) {
                    return;
                }
                photoUploadNotification.onPhotoUploadProgress(progress, total);
            }

            @Override
            public void onPhotoUploadProgressInfinite() {
                if (hasPhotoUploadStarted) photoUploadNotification.onPhotoUploadProgressInfinite();
            }

            @Override
            public void onPhotoUploadComplete() {
                if (hasPhotoUploadStarted) {
                    hasPhotoUploadStarted = false;
                    shouldUploadMedia = false;  // finished loading / uploading photos
                    photoUploadNotification.onPhotoUploadComplete();
                }
            }
        };
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void dropPostingNotification() {
        NotificationManagerCompat.from(this).cancel(Constant.NotificationID.POSTING);
    }

    private void dropPhotoUploadNotification() {
        NotificationManagerCompat.from(this).cancel(Constant.NotificationID.PHOTO_UPLOAD);
    }

    /**
     * PUT all {@param reports} that we have managed to obtain to repository.
     */
    private void persistReports(List<GroupReportEssence> reports, UseCase.OnPostExecuteCallback<GroupReportBundle> callback) {
        Timber.d("Persisting reports of size [%s / %s] to repository...", reports.size(), totalForPosting);
        if (reports.isEmpty()) {
            Timber.d("Nothing to be stored to repository");
            return;
        }

        PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(
                reports, keywordBundleId, currentPost.id());
        PutGroupReportBundle useCase = component.putGroupReportBundleUseCase();
        useCase.setPostExecuteCallback(callback);
        useCase.setParameters(parameters);
        useCase.execute();
    }

    /**
     * When Service has finished, it PUTs all 'reports' to repository and retrieves the id
     * of the corresponding newly created GroupReportBundle. Now it's time to pass this id
     * to notifications in order to open ReportScreen with proper input GroupReportBundle's
     * by these notifications.
     */
    @DebugLog
    private void updateGroupReportBundleIdForNotifications(long groupReportBundleId) {
        PendingIntent intent = PostingNotification.makePendingIntent(this, hasFinished,
                groupReportBundleId, keywordBundleId, currentPost.id());
        serviceNotificationBuilder.setContentIntent(intent);
        Notification notification = serviceNotificationBuilder.build();
        NotificationManagerCompat.from(this).notify(FOREGROUND_NOTIFICATION_ID, notification);

        // this call will automatically trigger notification manager to update posting notification
        postingNotification.updateGroupReportBundleId(this, groupReportBundleId);
    }

    /* Tasks in Service thread */
    // --------------------------------------------------------------------------------------------
    // prevents from execution of repeating tasks, if any (user can click buttons too quickly)
    private boolean alreadyQueuedTask(@TaskId int id) {
        for (Task task : tasks) {
            if (task.id == id) return true;
        }
        return false;
    }

    private void pollTasksAndRun() {
        while (!tasks.isEmpty()) {
            Task task = tasks.poll();
            if (task != null) {
                Timber.d("Running task with id: %s", task.id);
                task.runnable.run();  // performed in Service thread
            }
        }
    }

    private void wakeUp() {
        synchronized (lock) {
            lock.notify();
        }
    }

    private void leave() {
        synchronized (lock) {
            hasFinished = true;
            lock.notify();
        }
    }

    /**
     * Schedules to perform interruption in Service thread.
     */
    private void performInterruption() {
        if (alreadyQueuedTask(TASK_INTERRUPT)) return;

        tasks.add(new Task(TASK_INTERRUPT, () -> {
            if (shouldUploadMedia) {  // we are loading / uploading photos, wall posting will follow
                Timber.d("Interrupt while uploading photos");
                if (hasPhotoUploadStarted && photoUploadNotification != null) photoUploadNotification.onPhotoUploadInterrupt();
            } else {  // finished photo upload or completely avoided that
                Timber.d("Interrupt while wall posting");
                if (postingNotification != null) postingNotification.onPostingInterrupt();
            }
        }));

        wakeUp();
    }

    /**
     * Schedules to perform screen destroy reaction in Service thread.
     */
    private void performScreenDestroyed() {
        if (alreadyQueuedTask(TASK_SCREEN_DESTROY)) return;

        tasks.add(new Task(TASK_SCREEN_DESTROY, () -> {
            if (wasPaused) {
                dropPhotoUploadNotification();  // don't drop posting notification - we use it to access to stored reports
                Timber.d("Attempting to persist all report that Service has managed to retrieve");
                persistReports(storedReports, new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
                    @Override
                    public void onFinish(@Nullable GroupReportBundle bundle) {
                        if (bundle == null) {
                            Timber.e("[Screen destroy]: Failed to put new GroupReportBundle to repository - item not created, as expected");
                            throw new ProgramException();
                        }
                        Timber.i("Use-Case [Screen destroy]: succeeded to put GroupReportBundle");
                        if (postingNotification != null) postingNotification.onPostingComplete();
                        if (hasPhotoUploadStarted && photoUploadNotification != null) photoUploadNotification.onPhotoUploadComplete();
                        /**
                         * Here we update ids of notifications after call
                         * {@link PostingNotification#onPostingComplete()}, since we need to setup
                         * {@link PostingNotification#hasPostingFinished} flag properly before making
                         * a new pending intent with updated id and this flag.
                         *
                         * If both flag and id are properly set, notification will open ReportScreen
                         * in non-interactive mode with the most actual data.
                         */
                        updateGroupReportBundleIdForNotifications(bundle.id());
                        notifyPostingStatus(WALL_POSTING_STATUS_FINISHED);
                        Timber.d("Finishing service after ReportScreen destroyed...");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Use-Case [Screen destroy]: failed to put GroupReportBundle");
                        notifyPostingStatus(WALL_POSTING_STATUS_ERROR);
                    }
                });
            }
        }));

        wakeUp();
    }

    /**
     * Schedules to perform suspend in Service thread.
     */
    private void performSuspension(boolean paused) {
        @TaskId int taskId = paused ? TASK_SUSPEND_PAUSE : TASK_SUSPEND_RESUME;
        if (alreadyQueuedTask(taskId)) return;

        tasks.add(new Task(taskId, () -> {
            wasPaused = paused;

            if (shouldUploadMedia) {  // we are loading / uploading photos, wall posting will follow
                Timber.d("Suspend while uploading photos");
                if (paused) {
                    if (hasPhotoUploadStarted && photoUploadNotification != null) photoUploadNotification.onPhotoUploadPaused();
                    component.vkontakteEndpoint().pausePhotoUploading();
                } else {
                    // don't update notification, it will be auto-update via progress callback
                    component.vkontakteEndpoint().resumePhotoUploading();
                }
            } else {  // finished photo upload or completely avoided that
                Timber.d("Suspend while wall posting");
                if (paused) {
                    if (postingNotification != null) postingNotification.onPostingPaused();
                    component.vkontakteEndpoint().pauseWallPosting();
                } else {
                    // don't update notification, it will be auto-update via progress callback
                    component.vkontakteEndpoint().resumeWallPosting();
                }
            }
        }));

        wakeUp();
    }
}
