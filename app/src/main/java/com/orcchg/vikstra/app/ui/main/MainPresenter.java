package com.orcchg.vikstra.app.ui.main;

import android.app.Activity;
import android.content.Intent;

import com.orcchg.vikstra.app.ui.base.BaseCompositePresenter;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {

    private KeywordListPresenter keywordListPresenter;
    private PostSingleGridPresenter postSingleGridPresenter;
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
            long groupBundleId = Constant.BAD_ID;  // TODO: get id associated with keywords
            long keywordBundleId = keywordListPresenter.getSelectedKeywordBundleId();
            long postId = postSingleGridPresenter.getSelectedPostId();
            if (groupBundleId == Constant.BAD_ID) {
                getView().openGroupListScreen(keywordBundleId, postId);
            } else {
                makeWallPost();
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

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {}

    private void makeWallPost() {
//        Set<Long> selectedGroupIds = new TreeSet<>();  // exclude ids duplication
        // TODO: get GroupBundle by groupBundleId
        // TODO: get groups ids from associated GroupBundle
        // TODO: get Post by postId from Repository
//        vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroupIds, currentPost,
//                createMakeWallPostCallback(), getView(), getView());
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
//    private UseCase.OnPostExecuteCallback<List<GroupReport>> createMakeWallPostCallback() {
//        return new UseCase.OnPostExecuteCallback<List<GroupReport>>() {
//            @Override
//            public void onFinish(@Nullable List<GroupReport> values) {
//                if (isViewAttached()) getView().openReportScreen(  // TODO: SET PROPER GROUP-REPORT-iD
//                        Constant.BAD_ID, postSingleGridPresenter.getSelectedPostId());
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                if (isViewAttached()) getView().showError();
//            }
//        };
//    }
}
