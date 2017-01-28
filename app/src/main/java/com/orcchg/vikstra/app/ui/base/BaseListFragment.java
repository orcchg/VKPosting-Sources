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

    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            memento = restoreMemento(savedInstanceState);
        } else {
            memento = createMemento();
        }
        layoutManager = createLayoutManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isStateRestored() && memento.layoutManagerState != null) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.i("Restored state of layout manager");
            layoutManager.onRestoreInstanceState(memento.layoutManagerState);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                processListScroll(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        memento.layoutManagerState = layoutManager.onSaveInstanceState();
        memento.toBundle(outState);
        super.onSaveInstanceState(outState);
    }

    /* List helpers */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public RecyclerView getListView(int tag) {
        return recyclerView;
    }

    // {@see http://stackoverflow.com/questions/27841740/how-to-know-whether-a-recyclerview-linearlayoutmanager-is-scrolled-to-top-or-b/33515549#33515549}
    // ------------------------------------------
    protected boolean isListReachedTop() {
        int position = layoutManager.findFirstVisibleItemPosition();
        return position == 0 && layoutManager.findViewByPosition(position).getTop() == 0;
    }

    protected boolean isListReachedBottom() {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            int totalItems = adapter.getItemCount();
            return layoutManager.findLastVisibleItemPosition() == totalItems - 1;
        }
        Timber.w("Adapter must be supplied for the list to check whether bottom has been reached properly !");
        return false;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    protected abstract LinearLayoutManager createLayoutManager();

    void processListScroll(RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            onScrollTop();
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
    protected abstract void onScrollTop();
}
