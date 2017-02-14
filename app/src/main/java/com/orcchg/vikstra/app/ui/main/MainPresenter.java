package com.orcchg.vikstra.app.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseCompositePresenter;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListFragment;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.post.view.PostViewActivity;
import com.orcchg.vikstra.app.ui.viewobject.UserVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.UserToVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.User;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {
    private static final int PrID = Constant.PresenterId.MAIN_PRESENTER;

    private KeywordListPresenter keywordListPresenter;
    private PostSingleGridPresenter postSingleGridPresenter;

    private final GetGroupBundleById getGroupBundleByIdUseCase;  // TODO: unused
    private final GetPostById getPostByIdUseCase;
    private final UserToVoMapper userToVoMapper;
    private final VkontakteEndpoint vkontakteEndpoint;

    private Memento memento = new Memento();

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_IS_KEYWORD_BUNDLE_LOADED = "bundle_key_is_keyword_bundle_loaded_" + PrID;;
        private static final String BUNDLE_KEY_IS_KEYWORD_BUNDLE_SELECTED = "bundle_key_is_keyword_bundle_selected_" + PrID;;
        private static final String BUNDLE_KEY_IS_POST_LOADED = "bundle_key_is_post_loaded_" + PrID;
        private static final String BUNDLE_KEY_IS_POST_SELECTED = "bundle_key_is_post_selected_" + PrID;;
        private static final String BUNDLE_KEY_FLAG_RELOADED_ONCE = "bundle_key_flag_reloaded_once_" + PrID;

        private boolean isKeywordBundleLoaded;
        private boolean isKeywordBundleSelected;
        private boolean isPostLoaded;
        private boolean isPostSelected;
        private boolean reloadedOnce;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putBoolean(BUNDLE_KEY_IS_KEYWORD_BUNDLE_LOADED, isKeywordBundleLoaded);
            outState.putBoolean(BUNDLE_KEY_IS_KEYWORD_BUNDLE_SELECTED, isKeywordBundleSelected);
            outState.putBoolean(BUNDLE_KEY_IS_POST_LOADED, isPostLoaded);
            outState.putBoolean(BUNDLE_KEY_IS_POST_SELECTED, isPostSelected);
            outState.putBoolean(BUNDLE_KEY_FLAG_RELOADED_ONCE, reloadedOnce);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.isKeywordBundleLoaded = savedInstanceState.getBoolean(BUNDLE_KEY_IS_KEYWORD_BUNDLE_LOADED, false);
            memento.isKeywordBundleSelected = savedInstanceState.getBoolean(BUNDLE_KEY_IS_KEYWORD_BUNDLE_SELECTED, false);
            memento.isPostLoaded = savedInstanceState.getBoolean(BUNDLE_KEY_IS_POST_LOADED, false);
            memento.isPostSelected = savedInstanceState.getBoolean(BUNDLE_KEY_IS_POST_SELECTED, false);
            memento.reloadedOnce = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_RELOADED_ONCE, false);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Override
    protected List<? extends MvpPresenter> providePresenterList() {
        List<MvpPresenter> list = new ArrayList<>();
        list.add(keywordListPresenter);
        list.add(postSingleGridPresenter);
        return list;
    }

    @Inject
    MainPresenter(KeywordListPresenter keywordListPresenter, PostSingleGridPresenter postSingleGridPresenter,
                  GetGroupBundleById getGroupBundleByIdUseCase, GetPostById getPostByIdUseCase,
                  UserToVoMapper userToVoMapper, VkontakteEndpoint vkontakteEndpoint) {
        this.keywordListPresenter = keywordListPresenter;
        this.keywordListPresenter.setExternalValueEmitter(isSelected -> {
            memento.isKeywordBundleSelected = isSelected;
            long keywordBundleId = isSelected ? keywordListPresenter.getSelectedKeywordBundleId() : Constant.BAD_ID;
            if (isViewAttached()) {
                getView().showFab(memento.isKeywordBundleSelected && memento.isPostSelected);
                getView().updateKeywordBundleId(keywordBundleId);
            }
        });
        this.postSingleGridPresenter = postSingleGridPresenter;
        this.postSingleGridPresenter.setExternalValueEmitter(isSelected -> {
            memento.isPostSelected = isSelected;
            long postId = isSelected ? postSingleGridPresenter.getSelectedPostId() : Constant.BAD_ID;
            ContentUtility.CurrentSession.setLastSelectedPostId(postId);
            if (isViewAttached()) {
                getView().showFab(memento.isKeywordBundleSelected && memento.isPostSelected);
                getView().updatePostId(postId);
            }
        });
        this.getGroupBundleByIdUseCase = getGroupBundleByIdUseCase;
        this.getGroupBundleByIdUseCase.setPostExecuteCallback(createGetGroupBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.userToVoMapper = userToVoMapper;
        this.vkontakteEndpoint = vkontakteEndpoint;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onStart() {
        boolean freshStart = isOnFreshStart();
        super.onStart();
        if (!freshStart && !memento.reloadedOnce) {  // reload once on non-fresh start in order to expose showcase
            Timber.d("Reload list of Post-s and Keyword-s once on non-fresh start");
            memento.reloadedOnce = true;
            this.keywordListPresenter.setKeywordsLoadedEmitter(loaded -> {
                memento.isKeywordBundleLoaded = loaded;
                notifyBothListsHaveItems(memento.isKeywordBundleLoaded && memento.isPostLoaded);
            });
            this.postSingleGridPresenter.setPostsLoadedEmitter(loaded -> {
                memento.isPostLoaded = loaded;
                notifyBothListsHaveItems(memento.isKeywordBundleLoaded && memento.isPostLoaded);
            });

            retryKeywords();
            retryPosts();
        } else if (!freshStart) {
            Timber.d("Simple non-fresh start, nothing to be loaded");
            this.keywordListPresenter.setKeywordsLoadedEmitter(null);
            this.postSingleGridPresenter.setPostsLoadedEmitter(null);
        }
    }

    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GroupListActivity.REQUEST_CODE:  // keywords could change on GroupList screen
            case KeywordCreateActivity.REQUEST_CODE:
            case KeywordListActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) retryKeywords();  // refresh keywords list
                break;
            case PostCreateActivity.REQUEST_CODE:
            case PostViewActivity.REQUEST_CODE:  // PostViewScreen can update Post indirectly (via PostCreateScreen)
                if (resultCode == Activity.RESULT_OK) retryPosts();  // refresh posts grid
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onFabClick() {
        Timber.i("onFabClick");
        if (isViewAttached()) {
            long groupBundleId = keywordListPresenter.getSelectedGroupBundleId();
            long keywordBundleId = keywordListPresenter.getSelectedKeywordBundleId();
            long postId = postSingleGridPresenter.getSelectedPostId();
//            if (groupBundleId == Constant.BAD_ID) {
//                getView().openGroupListScreen(keywordBundleId, postId);
//            } else {
//                Timber.v("now get GroupBundle from repository, then get Post from repository");
//                getPostByIdUseCase.setPostId(postId);  // set proper id
//                getGroupBundleByIdUseCase.setGroupBundleId(groupBundleId);  // set proper id
//                getGroupBundleByIdUseCase.execute();
//            }
            if (keywordBundleId != Constant.BAD_ID && postId != Constant.BAD_ID) {
                getView().openGroupListScreen(keywordBundleId, postId);
            } else {
                getView().onKeywordBundleAndPostNotSelected();
            }
        }
    }

    @Override
    public void onScrollKeywordsList(int itemsLeftToEnd) {
        keywordListPresenter.onScroll(itemsLeftToEnd);
    }

    @Override
    public void onScrollPostsGrid(int itemsLeftToEnd) {
        postSingleGridPresenter.onScroll(itemsLeftToEnd);
    }

    // ------------------------------------------
    @Override
    public void logout() {
        VKSdk.logout();
        if (isViewAttached()) getView().onLoggedOut();
    }

    // ------------------------------------------
    @Override
    public void removeKeywordListItem(int position) {
        Timber.i("removeKeywordListItem");
        keywordListPresenter.removeListItem(position);
    }

    @Override
    public void removePostGridItem(int position) {
        Timber.i("removePostGridItem");
        postSingleGridPresenter.removeListItem(position);
    }

    @Override
    public void retryKeywords() {
        Timber.i("retryKeywords");
        keywordListPresenter.retry();
    }

    @Override
    public void retryPosts() {
        Timber.i("retryPosts");
        postSingleGridPresenter.retry();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        vkontakteEndpoint.getCurrentUser(createGetCurrentUserCallback());
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        freshStart();  // nothing to be restored
    }

    private void notifyBothListsHaveItems(boolean both) {
        if (isViewAttached() && both) getView().notifyBothListsHaveItems();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<User> createGetCurrentUserCallback() {
        return new UseCase.OnPostExecuteCallback<User>() {
            @Override
            public void onFinish(@Nullable User user) {
                if (user == null) {
                    Timber.e("Current user wasn't found, which is not possible once access token is valid");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get current User");
                UserVO viewObject = userToVoMapper.map(user);
                if (isViewAttached()) getView().showCurrentUser(viewObject);
            }

            @Override
            public void onError(Throwable reason) {
                Timber.e("Use-Case: failed to get current User");
                if (EndpointUtility.hasAccessTokenExhausted(reason)) {
                    Timber.w("Access Token has exhausted !");
                    if (isViewAttached()) getView().onAccessTokenExhausted();
                } else {
                    if (isViewAttached()) getView().showCurrentUser(null);
                }
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupBundle> createGetGroupBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.e("No GroupBundle found by id, which has improper value in selected KeywordBundle");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get GroupBundle by id");
                getPostByIdUseCase.execute();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get GroupBundle by id");
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                // TODO: handle NULL Post
                Timber.i("Use-Case: succeeded to get Post by id");
//                vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroups, post,
//                        createMakeWallPostCallback(), getView(), getView());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                // TODO: impl error properly
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

//    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
//        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
//            @DebugLog @Override
//            public void onFinish(@Nullable List<GroupReportEssence> reports) {
//                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(reports);
//                putGroupReportBundle.setParameters(parameters);
//                putGroupReportBundle.execute();
//            }
//
//            @DebugLog @Override
//            public void onError(Throwable e) {
//                sendPostingStartedMessage(false);
//                if (isViewAttached()) {
//                    getView().showError(GroupListFragment.RV_TAG);
//                }
//            }
//        };
//    }
}
