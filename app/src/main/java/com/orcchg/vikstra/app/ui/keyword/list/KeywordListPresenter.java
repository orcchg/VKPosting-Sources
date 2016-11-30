package com.orcchg.vikstra.app.ui.keyword.list;

import android.view.View;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordListItemMapper;
import com.orcchg.vikstra.domain.interactor.GetKeywordBundles;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class KeywordListPresenter extends BaseListPresenter<KeywordListContract.View>
        implements KeywordListContract.Presenter {

    private final GetKeywordBundles getKeywordBundlesUseCase;

    @Inject
    KeywordListPresenter(GetKeywordBundles getKeywordBundlesUseCase) {
        this.listAdapter = createListAdapter();
        this.getKeywordBundlesUseCase = getKeywordBundlesUseCase;
        this.getKeywordBundlesUseCase.setPostExecuteCallback(createGetKeywordBundlesCallback());
    }

    @Override
    protected BaseAdapter createListAdapter() {
        KeywordListAdapter adapter = new KeywordListAdapter();
        adapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener<KeywordListItemVO>() {
            @Override
            public void onItemClick(View view, KeywordListItemVO keywordListItemVO, int position) {
                Timber.d("Clicked item at position: " + position);
                // TODO: click
            }

            @Override
            public void onItemLongClick(View view, KeywordListItemVO keywordListItemVO, int position) {
                Timber.d("Clicked item at position: " + position);
                // TODO: long click, remove open screen

            }
        });
        return adapter;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onStart() {
        super.onStart();
        start();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retry() {
        start();
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        // TODO
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    public void start() {
        getKeywordBundlesUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<KeywordBundle>> createGetKeywordBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<KeywordBundle>>() {
            @Override
            public void onFinish(List<KeywordBundle> values) {
                memento.currentSize += values.size();
                KeywordListItemMapper mapper = new KeywordListItemMapper();
                List<KeywordListItemVO> vos = mapper.map(values);
                listAdapter.populate(vos, isThereMore());
                if (isViewAttached()) getView().showKeywords(vos);
            }

            @Override
            public void onError(Throwable e) {
                if (memento.currentSize <= 0) {
                    if (isViewAttached()) getView().showError();
                } else {
                    listAdapter.onError(true);
                }
            }
        };
    }
}
