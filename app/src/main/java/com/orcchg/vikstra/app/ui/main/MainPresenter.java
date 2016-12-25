package com.orcchg.vikstra.app.ui.main;

import android.app.Activity;
import android.content.Intent;

import com.orcchg.vikstra.app.ui.base.BaseCompositePresenter;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.post.create.PostCreateActivity;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.util.ContentUtility;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {

    private KeywordListPresenter keywordListPresenter;
    private PostSingleGridPresenter postSingleGridPresenter;

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
    MainPresenter(KeywordListPresenter keywordListPresenter, PostSingleGridPresenter postSingleGridPresenter) {
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
            if (isViewAttached()) getView().showFab(isKeywordBundleSelected && isPostSelected);
        });
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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
            long keywordBundleId = keywordListPresenter.getSelectedKeywordBundleId();
            long postId = postSingleGridPresenter.getSelectedPostId();
            getView().openGroupListScreen(keywordBundleId, postId);
        }
    }

    @Override
    public void onScrollKeywordsList(int itemsLeftToEnd) {
        keywordListPresenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {}

    /* Callback */
    // --------------------------------------------------------------------------------------------
}
