package com.orcchg.vikstra.app.ui.keyword.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordListItemMapper;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class KeywordListPresenter extends BaseListPresenter<KeywordListContract.View>
        implements KeywordListContract.Presenter {

    private final GetKeywordBundles getKeywordBundlesUseCase;

    private long selectedKeywordBundleId = Constant.BAD_ID;
    private final @KeywordListAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    @Inject
    public KeywordListPresenter(@KeywordListAdapter.SelectMode int selectMode, GetKeywordBundles getKeywordBundlesUseCase) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getKeywordBundlesUseCase = getKeywordBundlesUseCase;
        this.getKeywordBundlesUseCase.setPostExecuteCallback(createGetKeywordBundlesCallback());
    }

    public void setExternalValueEmitter(ValueEmitter<Boolean> listener) {
        externalValueEmitter = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        KeywordListAdapter adapter = new KeywordListAdapter(selectMode);
        adapter.setOnItemClickListener((view, keywordListItemVO, position) -> {
            changeSelectedKeywordBundleId(keywordListItemVO.getSelection() ? keywordListItemVO.id() : Constant.BAD_ID);
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
        changeSelectedKeywordBundleId(Constant.BAD_ID);  // drop selection
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

    protected void changeSelectedKeywordBundleId(long newId) {
        selectedKeywordBundleId = newId;
        if (externalValueEmitter != null) externalValueEmitter.emit(newId != Constant.BAD_ID);
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
                    Collections.sort(values);
                    memento.currentSize += values.size();
                    KeywordListItemMapper mapper = new KeywordListItemMapper();
                    List<KeywordListItemVO> vos = mapper.map(values);
                    // TODO: clearLastStoredInternalImageUrls list to prevent items duplication
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
