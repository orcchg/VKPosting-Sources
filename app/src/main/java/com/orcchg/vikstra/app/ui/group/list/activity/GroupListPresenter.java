package com.orcchg.vikstra.app.ui.group.list.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.post.OutConstants;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.list.PostListActivity;
import com.orcchg.vikstra.app.ui.util.ContextUtility;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.DumpGroups;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.util.Arrays;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static com.orcchg.vikstra.domain.util.Constant.BAD_ID;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {
    private static final int PrID = Constant.PresenterId.GROUP_LIST_ACTIVITY_PRESENTER;

    private final DumpGroups dumpGroupsUseCase;

    private Memento memento = new Memento();

    private GroupListMediatorComponent mediatorComponent;

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_EMAIL = "bundle_key_email_" + PrID;
        private static final String BUNDLE_KEY_TITLE = "bundle_key_title_" + PrID;
        private static final String BUNDLE_KEY_HAS_TITLE_CHANGED = "bundle_key_has_title_changed_" + PrID;

        private @Nullable String email;
        private @Nullable String title;
        private boolean hasTitleChanged;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putString(BUNDLE_KEY_EMAIL, email);
            outState.putString(BUNDLE_KEY_TITLE, title);
            outState.putBoolean(BUNDLE_KEY_HAS_TITLE_CHANGED, hasTitleChanged);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.email = savedInstanceState.getString(BUNDLE_KEY_EMAIL);
            memento.title = savedInstanceState.getString(BUNDLE_KEY_TITLE);
            memento.hasTitleChanged = savedInstanceState.getBoolean(BUNDLE_KEY_HAS_TITLE_CHANGED, false);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    GroupListPresenter(DumpGroups dumpGroupsUseCase) {
        this.dumpGroupsUseCase = dumpGroupsUseCase;
        this.dumpGroupsUseCase.setPostExecuteCallback(createDumpGroupsCallback());
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediatorComponent = DaggerGroupListMediatorComponent.builder()
                .groupListMediatorModule(new GroupListMediatorModule())
                .build();
        mediatorComponent.inject(this);
        mediatorComponent.mediator().attachFirst(this);
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PostCreateActivity.REQUEST_CODE:
            case PostListActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Timber.d("Post has been changed (and should be refreshed) resulting from screen with request code: %s", requestCode);
                    long postId = data.getLongExtra(OutConstants.OUT_EXTRA_POST_ID, Constant.BAD_ID);
                    if (isViewAttached()) getView().setNewPostId(postId);  // update initial postId on View to work properly further
                    sendPostHasChangedRequest(postId);  // update postId through Mediator to refresh Post via use-case
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediatorComponent.mediator().detachFirst();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addKeyword(Keyword keyword) {
        Timber.i("addKeyword: %s", keyword.toString());
        sendAddKeywordRequest(keyword);
    }

    @Override
    public void onDumpPressed() {
        Timber.i("onDumpPressed");
        long groupBundleId = sendAskForGroupBundleIdToDump();
        if (isViewAttached()) {
            if (groupBundleId != BAD_ID) {
                Timber.d("GroupBundle id [%s] is valid, ready to dump", groupBundleId);
                dumpGroupsUseCase.setParameters(new DumpGroups.Parameters(groupBundleId));
                if (AppConfig.INSTANCE.sendDumpFilesViaEmail()) {
                    Timber.d("Sending GroupBundle to email...");
                    getView().openEditDumpEmailDialog();
                } else {
                    Timber.d("Dumping GroupBundle to file...");
                    getView().openEditDumpFileNameDialog();
                }
            } else {
                Timber.d("GroupBundle is not available to dump");
                getView().openDumpNotReadyDialog();
            }
        }
    }

    @Override
    public void onFabClick() {
        Timber.i("onFabClick");
        sendPostToGroupsRequest();
    }

    @Override
    public void onPostThumbnailClick(long postId) {
        Timber.i("onPostThumbnailClick: %s", postId);
        if (isViewAttached()) {
            if (postId == Constant.BAD_ID) {
                // TODO: open PostCreate, if no posts at all in list
                // in case of initially empty post - open PostListScreen to choose Post among the existing ones
                getView().openPostListScreen();
            } else {
                // if Post has already been selected - open PostCreateScreen to edit the Post
                getView().openPostCreateScreen(postId);
            }
        }
    }

    @Override
    public void onTitleChanged(String text) {
        Timber.i("onTitleChanged: %s", text);
        memento.hasTitleChanged = !text.equals(memento.title);
        if (memento.hasTitleChanged) {
            memento.title = text;
            sendNewTitle(text);
        }
    }

    @DebugLog @Override
    public void performDumping(String path) {
        performDumping(path, null);
    }

    @DebugLog @Override
    public void performDumping(String path, @Nullable String email) {
        Timber.i("performDumping: path=%s, email=%s", path, email);
        memento.email = email;
        dumpGroupsUseCase.setPath(path);
        dumpGroupsUseCase.execute();
    }

    @Override
    public void retry() {
        Timber.i("retry");
        memento.hasTitleChanged = false;
        sendAskForRetry();
        freshStart();
    }

    @Override
    public void retryPost() {
        Timber.i("retryPost");
        sendAskForRetryPost();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void setPostingTimeout(int timeout) {
        sendPostingTimeout(timeout);
    }

    /* Mediator */
    // --------------------------------------------------------------------------------------------
    @Override
    public void receiveAddKeywordError() {
        if (isViewAttached()) getView().onAddKeywordError();
    }

    @Override
    public void receiveAlreadyAddedKeyword(String keyword) {
        if (isViewAttached()) getView().onAlreadyAddedKeyword(keyword);
    }

    @Override
    public String receiveAskForTitle() {
        if (isViewAttached()) return getView().getInputGroupsTitle();
        return ContextUtility.defaultTitle();
    }

    @Override
    public boolean receiveAskForTitleChanged() {
        return memento.hasTitleChanged;
    }

    @Override
    public void receiveEnableAddKeywordButtonRequest(boolean isEnabled) {
        if (isViewAttached()) getView().enableAddKeywordButton(isEnabled);
    }

    @Override
    public void receiveEmptyPost() {
        if (isViewAttached()) getView().showEmptyPost();
    }

    @Override
    public void receiveErrorPost() {
        if (isViewAttached()) getView().showErrorPost();
    }

    @Override
    public void receiveGroupBundleChanged() {
        if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
    }

    @Override
    public void receiveGroupsNotSelected() {
        if (isViewAttached()) getView().onGroupsNotSelected();
    }

    @Override
    public void receiveKeywordBundleChanged() {
        if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
    }

    @Override
    public void receiveKeywordsLimitReached(int limit) {
        if (isViewAttached()) getView().onKeywordsLimitReached(limit);
    }

    @Override
    public void receivePost(@Nullable PostSingleGridItemVO viewObject) {
        if (isViewAttached()) getView().showPost(viewObject);
    }

    @Override
    public void receivePostNotSelected() {
        if (isViewAttached()) getView().onPostNotSelected();
    }

    @Override
    public void receivePostingStartedMessage(boolean isStarted) {
        if (isViewAttached()) getView().showPostingStartedMessage(isStarted);
    }

    @Override
    public void receiveShowPostingButtonRequest(boolean isVisible) {
        if (isViewAttached()) getView().showPostingButton(isVisible);
    }

    @Override
    public void receiveUpdatedSelectedGroupsCounter(int newCount, int total) {
        if (isViewAttached()) getView().updateSelectedGroupsCounter(newCount, total);
    }

    @Override
    public void receiveUpdateTitleRequest(String newTitle) {
        if (isViewAttached()) getView().setInputGroupsTitle(newTitle);
    }

    // ------------------------------------------
    @Override
    public void sendAddKeywordRequest(Keyword keyword) {
        mediatorComponent.mediator().sendAddKeywordRequest(keyword);
    }

    @Override
    public long sendAskForGroupBundleIdToDump() {
        return mediatorComponent.mediator().sendAskForGroupBundleIdToDump();
    }

    @Override
    public void sendAskForRetry() {
        mediatorComponent.mediator().sendAskForRetry();
    }

    @Override
    public void sendAskForRetryPost() {
        mediatorComponent.mediator().sendAskForRetryPost();
    }

    @Override
    public void sendNewTitle(String newTitle) {
        mediatorComponent.mediator().sendNewTitle(newTitle);
    }

    @Override
    public void sendPostHasChangedRequest(long postId) {
        mediatorComponent.mediator().sendPostHasChangedRequest(postId);
    }

    @Override
    public void sendPostToGroupsRequest() {
        mediatorComponent.mediator().sendPostToGroupsRequest();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void sendPostingTimeout(int timeout) {
        mediatorComponent.mediator().sendPostingTimeout(timeout);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
    }

    @DebugLog
    private boolean hasChanges() {
        return memento.hasTitleChanged;
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        if (isViewAttached()) getView().setInputGroupsTitle(memento.title);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<String> createDumpGroupsCallback() {
        return new UseCase.OnPostExecuteCallback<String>() {
            @Override
            public void onFinish(@Nullable String path) {
                if (!TextUtils.isEmpty(path)) {
                    Timber.i("Use-Case: succeeded to dump Group-s");
                    if (AppConfig.INSTANCE.sendDumpFilesViaEmail() && !TextUtils.isEmpty(memento.email)) {
                        Timber.d("Group-s have been dumped to file [%s]. Now send it via email", path);
                        String[] recipients = memento.email.split(",");
                        EmailContent.Builder builder = EmailContent.builder()
                                .setAttachment(FileUtility.uriFromFile(path))
                                .setRecipients(Arrays.asList(recipients));
                        if (isViewAttached()) getView().openEmailScreen(builder);
                    } else {
                        if (isViewAttached()) getView().showDumpSuccess(path);
                    }
                } else {
                    Timber.e("Use-Case: failed to dump Group-s");
                    if (isViewAttached()) getView().showDumpError();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to dump Group-s");
                if (isViewAttached()) getView().showDumpError();
            }
        };
    }
}
