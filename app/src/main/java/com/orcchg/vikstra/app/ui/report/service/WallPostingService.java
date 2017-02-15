package com.orcchg.vikstra.app.ui.report.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.common.notification.PhotoUploadNotification;
import com.orcchg.vikstra.app.ui.common.notification.PostingNotification;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.notification.IPhotoUploadNotificationDelegate;
import com.orcchg.vikstra.domain.notification.IPostingNotificationDelegate;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class WallPostingService extends IntentService {
    public static final String NAME = "wall_posting_service";
    public static final String EXTRA_KEYWORD_BUNDLE_ID = "extra_keyword_bundle_id";
    public static final String EXTRA_SELECTED_GROUPS = "extra_selected_groups";
    public static final String EXTRA_CURRENT_POST = "extra_current_post";

    private long keywordBundleId = Constant.BAD_ID;
    private Post currentPost;

    private final PutGroupReportBundle putGroupReportBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    private PostingNotification postingNotification;
    private PhotoUploadNotification photoUploadNotification;

    public WallPostingService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.i("Enter Wall Posting service");

        ArrayList<Group> selectedGroups = intent.getParcelableArrayListExtra(EXTRA_SELECTED_GROUPS);
        keywordBundleId = intent.getLongExtra(EXTRA_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
        currentPost = intent.getParcelableExtra(EXTRA_CURRENT_POST);

        initNotifications();

        vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroups, currentPost,
                createMakeWallPostCallback(), postingDelegate(), photoUploadDelegate());
    }

    /* Notification delegate */
    // --------------------------------------------------------------------------------------------
    private void initNotifications() {
        postingNotification = new PostingNotification(this, groupReportBundleId, keywordBundleId, currentPost.id());
        photoUploadNotification = new PhotoUploadNotification(this);
    }

    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                Timber.i("Use-Case: succeeded to make wall posting");
                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(
                        reports, keywordBundleId, currentPost.id());
                putGroupReportBundleUseCase.setParameters(parameters);
                putGroupReportBundleUseCase.execute();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to make wall posting");
                sendPostingStartedMessage(false);
                // TODO: error on wall posting properly
            }
        };
    }

    // --------------------------------------------------------------------------------------------
    private IPostingNotificationDelegate postingDelegate() {
        return new IPostingNotificationDelegate() {
            @Override
            public void onPostingProgress(int progress, int total) {
                //
            }

            @Override
            public void onPostingProgressInfinite() {
                //
            }

            @Override
            public void onPostingComplete() {
                //
            }
        };
    }

    private IPhotoUploadNotificationDelegate photoUploadDelegate() {
        return new IPhotoUploadNotificationDelegate() {
            @Override
            public void onPhotoUploadProgress(int progress, int total) {
                //
            }

            @Override
            public void onPhotoUploadProgressInfinite() {
                //
            }

            @Override
            public void onPhotoUploadComplete() {
                //
            }
        };
    };
}
