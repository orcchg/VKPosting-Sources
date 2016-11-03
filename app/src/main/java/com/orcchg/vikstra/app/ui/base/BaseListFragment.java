package com.orcchg.vikstra.app.ui.base;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseListFragment<V extends MvpView, P extends MvpPresenter<V>> extends BaseFragment<V, P>
        implements MvpListView {

    protected RecyclerView recyclerView;
    protected LinearLayoutManager layoutManager;

    private int lastVisible = -1;

    protected static class Memento {
        protected static final String BUNDLE_KEY_LM_STATE = "bundle_key_lm_state";

        protected Parcelable layoutManagerState;

        protected void toBundle(@NonNull Bundle outState) {
            outState.putParcelable(BUNDLE_KEY_LM_STATE, layoutManagerState);
        }

        protected static Memento fromBundle(@NonNull Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.layoutManagerState = savedInstanceState.getParcelable(BUNDLE_KEY_LM_STATE);
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            memento = restoreMemento(savedInstanceState);
        } else {
            memento = createMemento();
        }
        layoutManager = new LinearLayoutManager(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isStateRestored() && memento.layoutManagerState != null) {
            Timber.i("Restored state of layout manager");
            layoutManager.onRestoreInstanceState(memento.layoutManagerState);
        }
        recyclerView.setLayoutManager(layoutManager);
    }

    @DebugLog @Override
    public void onSaveInstanceState(Bundle outState) {
        memento.layoutManagerState = layoutManager.onSaveInstanceState();
        memento.toBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public RecyclerView getListView() {
        return recyclerView;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void processListScroll(RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            return;  // skip scroll up
        }

        int last = layoutManager.findLastVisibleItemPosition();
        if (lastVisible == last) {
            return;  // skip scroll due to layout
        }

        lastVisible = last;
        int total = layoutManager.getItemCount();
        onScroll(total - last);
    }

    protected abstract void onScroll(int itemsLeftToEnd);
}
