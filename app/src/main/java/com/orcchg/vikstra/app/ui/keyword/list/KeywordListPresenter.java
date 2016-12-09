package com.orcchg.vikstra.app.ui.keyword.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordListItemMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class KeywordListPresenter extends BaseListPresenter<KeywordListContract.View>
        implements KeywordListContract.Presenter {

    private final GetKeywordBundles getKeywordBundlesUseCase;

    private long selectedKeywordBundleId = Constant.BAD_ID;
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> externalItemClickListener;

    @Inject
    KeywordListPresenter(GetKeywordBundles getKeywordBundlesUseCase) {
        this.listAdapter = createListAdapter();
        this.getKeywordBundlesUseCase = getKeywordBundlesUseCase;
        this.getKeywordBundlesUseCase.setPostExecuteCallback(createGetKeywordBundlesCallback());
    }

    public void setExternalItemClickListener(BaseAdapter.OnItemClickListener<KeywordListItemVO> listener) {
        externalItemClickListener = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        KeywordListAdapter adapter = new KeywordListAdapter();
        adapter.setOnItemClickListener((view, keywordListItemVO, position) -> {
            // TODO: impl multi-selection or not?
            selectedKeywordBundleId = keywordListItemVO.getSelection() ? keywordListItemVO.id() : Constant.BAD_ID;
            if (externalItemClickListener != null) externalItemClickListener.onItemClick(view, keywordListItemVO, position);
        });
        adapter.setOnItemLongClickListener((view, keywordListItemVO, position) -> {
            // TODO: impl long click
        });
        adapter.setOnEditClickListener((view, keywordListItemVO, position) -> {
            if (isViewAttached()) getView().openKeywordCreateScreen(keywordListItemVO.id());
        });
        return adapter;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retry() {
        listAdapter.clear();
        dropListStat();
        freshStart();
    }

    @Override
    public void onScroll(int itemsLeftToEnd) {
        // TODO
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    public long getSelectedKeywordBundleId() {
        return selectedKeywordBundleId;
    }

    @DebugLog @Override
    protected void freshStart() {
        getKeywordBundlesUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<KeywordBundle>> createGetKeywordBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<KeywordBundle>>() {
            @Override
            public void onFinish(@Nullable List<KeywordBundle> values) {
                // TODO: NPE
                if (values == null || values.isEmpty()) {
                    if (isViewAttached()) getView().showEmptyList();
                } else {
                    memento.currentSize += values.size();
                    KeywordListItemMapper mapper = new KeywordListItemMapper();
                    List<KeywordListItemVO> vos = mapper.map(values);
                    // TODO: clear list to prevent items duplication
                    listAdapter.populate(vos, isThereMore());
                    if (isViewAttached()) getView().showKeywords(vos);
                }
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
