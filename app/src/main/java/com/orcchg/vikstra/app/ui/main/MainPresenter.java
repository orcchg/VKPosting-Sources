package com.orcchg.vikstra.app.ui.main;

import android.app.Activity;
import android.content.Intent;
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
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {

    private KeywordListPresenter keywordListPresenter;
    private PostSingleGridPresenter postSingleGridPresenter;

    private final GetGroupBundleById getGroupBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    private boolean isKeywordBundleSelected;  // TODO: save instance state
    private boolean isPostSelected;

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
                  VkontakteEndpoint vkontakteEndpoint) {
        this.keywordListPresenter = keywordListPresenter;
        this.keywordListPresenter.setExternalValueEmitter(isSelected -> {
            isKeywordBundleSelected = isSelected;
            if (isViewAttached()) getView().showFab(isKeywordBundleSelected && isPostSelected);
        });
        this.postSingleGridPresenter = postSingleGridPresenter;
        this.postSingleGridPresenter.setExternalValueEmitter(isSelected -> {
            isPostSelected = isSelected;
            long postId = isSelected ? postSingleGridPresenter.getSelectedPostId() : Constant.BAD_ID;
            ContentUtility.CurrentSession.setLastSelectedPostId(postId);
            if (isViewAttached()) {
                getView().showFab(isKeywordBundleSelected && isPostSelected);
                getView().updatePostId(postId);
            }
        });
        this.getGroupBundleByIdUseCase = getGroupBundleByIdUseCase;
        this.getGroupBundleByIdUseCase.setPostExecuteCallback(createGetGroupBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.vkontakteEndpoint = vkontakteEndpoint;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
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
                if (resultCode == Activity.RESULT_OK) retryPosts();  // refresh posts grid
                break;
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retryKeywords() {
        keywordListPresenter.retry();
    }

    @DebugLog @Override
    public void retryPosts() {
        postSingleGridPresenter.retry();
    }

    @Override
    public void onFabClick() {
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
            getView().openGroupListScreen(keywordBundleId, postId);
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

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {}

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<GroupBundle> createGetGroupBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.e("No GroupBundle found by id, which has improper value in selected KeywordBundle");
                    throw new ProgramException();
                }
                Timber.v("Fetched GroupBundle, now get Post from repository, then make wall post");
                getPostByIdUseCase.execute();
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                // TODO: handle NULL Post
                Timber.v("Fetched Post, now make wall post");
//                vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroups, post,
//                        createMakeWallPostCallback(), getView(), getView());
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

//    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
//        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
//            @Override
//            public void onFinish(@Nullable List<GroupReportEssence> reports) {
//                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(reports);
//                putGroupReportBundle.setParameters(parameters);
//                putGroupReportBundle.execute();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                sendPostingStartedMessage(false);
//                if (isViewAttached()) {
//                    getView().showError(GroupListFragment.RV_TAG);
//                }
//            }
//        };
//    }
}
