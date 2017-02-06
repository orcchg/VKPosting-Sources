package com.orcchg.vikstra.app.ui.keyword.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.keyword.create.KeywordCreateActivity;
import com.orcchg.vikstra.app.ui.util.ValueEmitter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordBundleToVoMapper;
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.DeleteKeywordBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.parcelable.ParcelableKeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class KeywordListPresenter extends BaseListPresenter<KeywordListContract.View>
        implements KeywordListContract.Presenter {
    private static final int PrID = Constant.PresenterId.KEYWORD_LIST_PRESENTER;

    private final GetKeywordBundles getKeywordBundlesUseCase;
    private final DeleteKeywordBundle deleteKeywordBundleUseCase;

    private Memento memento = new Memento();

    private final @BaseSelectAdapter.SelectMode int selectMode;
    private ValueEmitter<Boolean> externalValueEmitter;

    private final KeywordBundleToVoMapper keywordBundleToVoMapper;

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_KEYWORD_BUNDLES = "bundle_key_keyword_bundles_" + PrID;
        private static final String BUNDLE_KEY_SELECTED_GROUP_BUNDLE_ID = "bundle_key_selected_group_bundle_id_" + PrID;
        private static final String BUNDLE_KEY_SELECTED_KEYWORD_BUNDLE_ID = "bundle_key_selected_keyword_bundle_id_" + PrID;
        private static final String BUNDLE_KEY_SELECTED_LIST_MODEL_POSITION = "bundle_key_selected_model_position_" + PrID;
        private static final String BUNDLE_KEY_WAS_LIST_ITEM_SELECTED = "bundle_key_was_list_item_selected_" + PrID;

        private List<KeywordBundle> keywordBundles = new ArrayList<>();
        private long selectedGroupBundleId = Constant.BAD_ID;
        private long selectedKeywordBundleId = Constant.BAD_ID;
        private int selectedModelPosition = Constant.BAD_POSITION;
        private boolean wasListItemSelected = false;

        @DebugLog
        private void toBundle(Bundle outState) {
            ArrayList<ParcelableKeywordBundle> parcelables = new ArrayList<>();
            for (KeywordBundle item : keywordBundles) {
                parcelables.add(new ParcelableKeywordBundle(item));
            }
            outState.putParcelableArrayList(BUNDLE_KEY_KEYWORD_BUNDLES, parcelables);
            outState.putLong(BUNDLE_KEY_SELECTED_GROUP_BUNDLE_ID, selectedGroupBundleId);
            outState.putLong(BUNDLE_KEY_SELECTED_KEYWORD_BUNDLE_ID, selectedKeywordBundleId);
            outState.putInt(BUNDLE_KEY_SELECTED_LIST_MODEL_POSITION, selectedModelPosition);
            outState.putBoolean(BUNDLE_KEY_WAS_LIST_ITEM_SELECTED, wasListItemSelected);
        }

        @DebugLog
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.keywordBundles = new ArrayList<>();
            ArrayList<ParcelableKeywordBundle> parcelables = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_KEYWORD_BUNDLES);
            if (parcelables != null) {
                for (ParcelableKeywordBundle item : parcelables) {
                    memento.keywordBundles.add(item.get());
                }
            }
            memento.selectedGroupBundleId = savedInstanceState.getLong(BUNDLE_KEY_SELECTED_GROUP_BUNDLE_ID, Constant.BAD_ID);
            memento.selectedKeywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_SELECTED_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            memento.selectedModelPosition = savedInstanceState.getInt(BUNDLE_KEY_SELECTED_LIST_MODEL_POSITION, Constant.BAD_POSITION);
            memento.wasListItemSelected = savedInstanceState.getBoolean(BUNDLE_KEY_WAS_LIST_ITEM_SELECTED, false);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    public KeywordListPresenter(@BaseSelectAdapter.SelectMode int selectMode,
            GetKeywordBundles getKeywordBundlesUseCase, DeleteKeywordBundle deleteKeywordBundleUseCase,
            KeywordBundleToVoMapper keywordBundleToVoMapper) {
        this.selectMode = selectMode;
        this.listAdapter = createListAdapter();
        this.getKeywordBundlesUseCase = getKeywordBundlesUseCase;
        this.getKeywordBundlesUseCase.setPostExecuteCallback(createGetKeywordBundlesCallback());
        this.deleteKeywordBundleUseCase = deleteKeywordBundleUseCase;  // no callback - background task
        this.keywordBundleToVoMapper = keywordBundleToVoMapper;
    }

    public void setExternalValueEmitter(ValueEmitter<Boolean> listener) {
        externalValueEmitter = listener;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        KeywordListAdapter adapter = new KeywordListAdapter(selectMode);
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            memento.selectedModelPosition = viewObject.getSelection() ? position : Constant.BAD_POSITION;
            memento.wasListItemSelected = viewObject.getSelection();
            long groupBundleId = viewObject.getSelection() ? viewObject.groupBundleId() : Constant.BAD_ID;
            long keywordBundleId = viewObject.getSelection() ? viewObject.id() : Constant.BAD_ID;
            changeSelectedGroupAndKeywordBundleId(groupBundleId, keywordBundleId);
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
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return KeywordListFragment.RV_TAG;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GroupListActivity.REQUEST_CODE:  // keywords could change on GroupListScreen
            case KeywordCreateActivity.REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    Timber.d("List of Keyword-s has been changed on KeywordListScreen resulting from screen with request code: %s", requestCode);
                    retry();  // refresh keywords list
                    if (isViewAttached()) getView().setCloseViewResult(Activity.RESULT_OK);
                }
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
    public void removeListItem(int position) {
        Timber.i("removeListItem: %s", position);
        long keywordBundleId = memento.keywordBundles.get(position).id();
        deleteKeywordBundleUseCase.setKeywordBundleId(keywordBundleId);
        deleteKeywordBundleUseCase.execute();  // silent delete without callback

        memento.keywordBundles.remove(position);
        listAdapter.remove(position);

        if (memento.keywordBundles.isEmpty()) {
            dropSelection();
            if (isViewAttached()) getView().showEmptyList(getListTag());
        }
    }

    @Override
    public void retry() {
        Timber.i("retry");
        deleteKeywordBundleUseCase.setKeywordBundleId(Constant.BAD_ID);
        memento.keywordBundles.clear();
        listAdapter.clear();
        dropListStat();
        dropSelection();
        freshStart();
    }

    /* List */
    // ------------------------------------------
    @Override
    protected void onLoadMore() {
        // TODO: on load more
    }

    private void retryLoadMore() {
        listAdapter.onError(false); // show loading more
        // TODO: load more limit-offset
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    public long getSelectedGroupBundleId() {
        return memento.selectedGroupBundleId;
    }

    @DebugLog
    public long getSelectedKeywordBundleId() {
        return memento.selectedKeywordBundleId;
    }

    @DebugLog
    private boolean changeSelectedGroupAndKeywordBundleId(long groupBundleId, long keywordBundleId) {
        memento.selectedGroupBundleId = groupBundleId;
        memento.selectedKeywordBundleId = keywordBundleId;
        if (externalValueEmitter != null) {
            /**
             * keywordBundleId always differs from {@link Constant.BAD_ID} for valid {@link KeywordBundle}
             * instance - the only kind of instances allowed to populate the list.
             *
             * groupBundleId could be {@link Constant.BAD_ID}, which just means that no
             * {@link com.orcchg.vikstra.domain.model.GroupBundle} instance is associated with
             * selected {@link KeywordBundle} instance.
             */
            externalValueEmitter.emit(keywordBundleId != Constant.BAD_ID);
            return true;
        }
        return false;
    }

    private void dropSelection() {
        changeSelectedGroupAndKeywordBundleId(Constant.BAD_ID, Constant.BAD_ID);  // drop selection
        memento.selectedModelPosition = Constant.BAD_POSITION;
        memento.wasListItemSelected = false;
    }

    public boolean isEmpty() {
        return memento.keywordBundles.isEmpty();
    }

    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(getListTag());
        getKeywordBundlesUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        int position = memento.selectedModelPosition;
        boolean isEmpty = populateList(memento.keywordBundles);
        if (!isEmpty && position != Constant.BAD_POSITION) {
            ((KeywordListAdapter) listAdapter).selectItemAtPosition(position, memento.wasListItemSelected);
        }
        changeSelectedGroupAndKeywordBundleId(memento.selectedGroupBundleId, memento.selectedKeywordBundleId);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private UseCase.OnPostExecuteCallback<List<KeywordBundle>> createGetKeywordBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<KeywordBundle>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<KeywordBundle> bundles) {
                if (bundles == null) {
                    Timber.e("List of KeywordBundle-s must not be null, it could be empty at least");
                    throw new ProgramException();
                } else if (bundles.isEmpty()) {
                    Timber.i("Use-Case: succeeded to get list of KeywordBundle-s");
                    if (isViewAttached()) getView().showEmptyList(getListTag());
                } else {
                    Timber.i("Use-Case: succeeded to get list of KeywordBundle-s");
                    Collections.sort(bundles);
                    memento.keywordBundles = bundles;
                    listMemento.currentSize += bundles.size();
                    populateList(bundles);
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of KeywordBundle-s");
                if (listMemento.currentSize <= 0) {
                    if (isViewAttached()) getView().showError(getListTag());
                } else {
                    listAdapter.onError(true);
                }
            }
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private boolean populateList(List<KeywordBundle> bundles) {
        List<KeywordListItemVO> vos = keywordBundleToVoMapper.map(bundles);
        listAdapter.clearSilent();  // TODO: take load-more into account later
        listAdapter.populate(vos, isThereMore());
        boolean isEmpty = vos == null || vos.isEmpty();
        if (isViewAttached()) getView().showKeywords(isEmpty);
        return isEmpty;
    }

    // TODO: assign totalItems
}
