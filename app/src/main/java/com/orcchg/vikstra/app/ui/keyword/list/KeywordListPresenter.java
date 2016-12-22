package com.orcchg.vikstra.app.ui.keyword.list;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordBundleToVoMapper;
import com.orcchg.vikstra.app.util.ContentUtility;
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
    private final @BaseSelectAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    final KeywordBundleToVoMapper keywordBundleToVoMapper;

    @Inject
    public KeywordListPresenter(@BaseSelectAdapter.SelectMode int selectMode,
            GetKeywordBundles getKeywordBundlesUseCase, KeywordBundleToVoMapper keywordBundleToVoMapper) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getKeywordBundlesUseCase = getKeywordBundlesUseCase;
        this.getKeywordBundlesUseCase.setPostExecuteCallback(createGetKeywordBundlesCallback());
        this.keywordBundleToVoMapper = keywordBundleToVoMapper;
    }

    public void setExternalValueEmitter(ValueEmitter<Boolean> listener) {
        externalValueEmitter = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        KeywordListAdapter adapter = new KeywordListAdapter(selectMode);
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            changeSelectedKeywordBundleId(viewObject.getSelection() ? viewObject.id() : Constant.BAD_ID);
        });
        adapter.setOnItemLongClickListener((view, viewObject, position) -> {
            // TODO: impl long click
        });
        adapter.setOnEditClickListener((view, viewObject, position) -> {
            if (isViewAttached()) {
                if (viewObject.groupBundleId() == Constant.BAD_ID) {
                    getView().openKeywordCreateScreen(viewObject.id());
                } else {
                    long postId = ContentUtility.CurrentSession.getLastSelectedPostId();
                    getView().openGroupListScreen(viewObject.id(), postId);
                }
            }
        });
        return adapter;
    }

    @Override
    protected int getListTag() {
        return KeywordListFragment.RV_TAG;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
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
    public long getSelectedKeywordBundleId() {
        return selectedKeywordBundleId;
    }

    @Override
    protected void freshStart() {
        getKeywordBundlesUseCase.execute();
    }

    @DebugLog
    protected boolean changeSelectedKeywordBundleId(long newId) {
        selectedKeywordBundleId = newId;
        if (externalValueEmitter != null) {
            externalValueEmitter.emit(newId != Constant.BAD_ID);
            return true;
        }
        return false;
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<KeywordBundle>> createGetKeywordBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<KeywordBundle>>() {
            @Override
            public void onFinish(@Nullable List<KeywordBundle> values) {
                if (values == null || values.isEmpty()) {
                    if (isViewAttached()) getView().showEmptyList();
                } else {
                    Collections.sort(values);
                    memento.currentSize += values.size();
                    List<KeywordListItemVO> vos = keywordBundleToVoMapper.map(values);
                    listAdapter.populate(vos, isThereMore());
                    if (isViewAttached()) getView().showKeywords(vos == null || vos.isEmpty());
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
