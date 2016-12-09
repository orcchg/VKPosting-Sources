package com.orcchg.vikstra.app.ui.main;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.BaseCompositePresenter;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.post.single.PostSingleGridPresenter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {

    KeywordListPresenter keywordListPresenter;
    PostSingleGridPresenter postSingleGridPresenter;  // not added to list, fragment handles it automatically

    @Override
    protected List<? extends MvpPresenter> providePresenterList() {
        List<MvpPresenter> list = new ArrayList<>();
        list.add(keywordListPresenter);
        return list;
    }

    @Inject
    MainPresenter(KeywordListPresenter keywordListPresenter, PostSingleGridPresenter postSingleGridPresenter) {
        this.keywordListPresenter = keywordListPresenter;
        this.keywordListPresenter.setExternalItemClickListener(createExternalItemClickCallback());
        this.postSingleGridPresenter = postSingleGridPresenter;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void retry() {
        keywordListPresenter.retry();
    }

    @Override
    public void onFabClick() {
        if (isViewAttached()) getView().openGroupListScreen(keywordListPresenter.getSelectedKeywordBundleId());
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        keywordListPresenter.onScroll(itemsLeftToEnd);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {}

    /* Callback */
    // ------------------------------------------
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> createExternalItemClickCallback() {
        return new BaseAdapter.OnItemClickListener<KeywordListItemVO>() {
            @Override
            public void onItemClick(View view, KeywordListItemVO keywordListItemVO, int position) {
                // TODO: use both Post and Keywords selected to show fab
                boolean postSelected = true;
                boolean keywordsSelected = keywordListItemVO.getSelection();
                if (isViewAttached()) getView().showFab(postSelected && keywordsSelected);
            }
        };
    }
}
