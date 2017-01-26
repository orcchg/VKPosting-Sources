package com.orcchg.vikstra.app.ui.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.common.screen.ListPresenter;
import com.orcchg.vikstra.domain.DomainConfig;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseListPresenter<V extends MvpListView> extends BasePresenter<V> implements ListPresenter {

    protected BaseAdapter listAdapter;

    protected abstract BaseAdapter createListAdapter();
    protected abstract int getListTag();
    protected abstract void onLoadMore();

    protected static class Memento {
        protected static final String BUNDLE_KEY_CURRENT_SIZE = "bundle_key_current_size";
        protected static final String BUNDLE_KEY_CURRENT_OFFSET = "bundle_key_current_offset";
        protected static final String BUNDLE_KEY_TOTAL_ITEMS = "bundle_key_total_items";

        public int currentSize = 0;
        public int currentOffset = 0;
        public int totalItems = 0;

        protected void toBundle(Bundle outState) {
            outState.putInt(BUNDLE_KEY_CURRENT_SIZE, currentSize);
            outState.putInt(BUNDLE_KEY_CURRENT_OFFSET, currentOffset);
            outState.putInt(BUNDLE_KEY_TOTAL_ITEMS, totalItems);
        }

        protected static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.currentSize = savedInstanceState.getInt(BUNDLE_KEY_CURRENT_SIZE);
            memento.currentOffset = savedInstanceState.getInt(BUNDLE_KEY_CURRENT_OFFSET);
            memento.totalItems = savedInstanceState.getInt(BUNDLE_KEY_TOTAL_ITEMS);
            return memento;
        }
    }

    protected Memento memento;

    protected Memento createMemento() {
        return new Memento();
    }

    protected Memento restoreMemento(@NonNull Bundle savedInstanceState) {
        return Memento.fromBundle(savedInstanceState);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (listAdapter == null) {
            String message = "Concrete method createListAdapter() must be called from subclass Ctor first!";
            Timber.e(message);
            throw new IllegalStateException(message);
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            memento = restoreMemento(savedInstanceState);
        } else {
            memento = createMemento();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isViewAttached()) {
            RecyclerView list = getView().getListView(getListTag());
            if (list.getAdapter() == null) {
                list.setAdapter(listAdapter);
            }
        } else {
            Timber.w("No View is attached");
        }
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onScroll(int itemsLeftToEnd) {
        if (isThereMore() && itemsLeftToEnd <= DomainConfig.INSTANCE.loadMoreThreshold) {
            memento.currentOffset += DomainConfig.INSTANCE.limitItemsPerRequest;
            onLoadMore();
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected void dropListStat() {
        memento.currentSize = 0;
        memento.currentOffset = 0;
        memento.totalItems = 0;
    }

    @DebugLog
    protected boolean isThereMore() {
        return memento.totalItems > memento.currentSize + memento.currentOffset;
    }
}
