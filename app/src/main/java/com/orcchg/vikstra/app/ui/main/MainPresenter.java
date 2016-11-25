package com.orcchg.vikstra.app.ui.main;

import com.orcchg.vikstra.app.ui.base.BaseCompositePresenter;
import com.orcchg.vikstra.app.ui.base.MvpPresenter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.post.list.PostListPresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MainPresenter extends BaseCompositePresenter<MainContract.View> implements MainContract.Presenter {

    KeywordListPresenter keywordListPresenter;
    PostListPresenter postListPresenter;

    @Override
    protected List<? extends MvpPresenter> providePresenterList() {
        List<MvpPresenter> list = new ArrayList<>();
        list.add(keywordListPresenter);
        list.add(postListPresenter);
        return list;
    }

    @Inject
    MainPresenter(KeywordListPresenter keywordListPresenter, PostListPresenter postListPresenter) {
        this.keywordListPresenter = keywordListPresenter;
        this.postListPresenter = postListPresenter;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void retry() {
        keywordListPresenter.retry();
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        keywordListPresenter.onScroll(itemsLeftToEnd);
    }
}
