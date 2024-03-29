package com.orcchg.vikstra.app.ui.base.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.ErrorViewHolder;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.LoadingViewHolder;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseAdapter<ModelViewHolder extends NormalViewHolder<Model>, Model> extends RecyclerView.Adapter<BaseViewHolder> {
    public interface OnItemClickListener<Model> {
        void onItemClick(View view, Model model, int position);
    }

    public interface OnItemLongClickListener<Model> {
        void onItemLongClick(View view, Model model, int position);
    }

    protected static final int VIEW_TYPE_NORMAL = 0;
    protected static final int VIEW_TYPE_LOADING = 1;
    protected static final int VIEW_TYPE_ERROR = 2;

    protected final List<Model> models;
    protected boolean isThereMore = false;
    protected boolean isInError = false;

    protected OnItemClickListener<Model> onItemClickListener;
    protected OnItemLongClickListener<Model> onItemLongClickListener;
    protected View.OnClickListener onErrorClickListener;

    public BaseAdapter() {
        models = new ArrayList<>();
    }

    @Nullable @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:   return createModelViewHolder(parent);
            case VIEW_TYPE_LOADING:  return createLoadingViewHolder(parent);
            case VIEW_TYPE_ERROR:    return createErrorViewHolder(parent);
            default:                 return null;
        }
    }

    @Override @SuppressWarnings("unchecked")
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_NORMAL:
                ((ModelViewHolder) holder).bind(getItemAtPosition(position));
                break;
        }
    }

    protected Model getItemAtPosition(int position) {
        return models.get(position);
    }

    @Override
    public int getItemCount() {
        return models.isEmpty() ? 0 : models.size() + (isThereMore ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        boolean isLoading = isThereMore && position == getItemCount() - 1;
        return isLoading ? (isInError ? VIEW_TYPE_ERROR : VIEW_TYPE_LOADING) : VIEW_TYPE_NORMAL;
    }

    /* Error state */
    // --------------------------------------------------------------------------------------------
    public void setOnErrorClickListener(View.OnClickListener onErrorClickListener) {
        this.onErrorClickListener = onErrorClickListener;
    }

    public void onError(boolean isInError) {
        if (!isThereMore) return;
        this.isInError = isInError;
        notifyItemChanged(getItemCount() - 1);
    }

    /* Data access */
    // --------------------------------------------------------------------------------------------
    public void setOnItemClickListener(OnItemClickListener<Model> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<Model> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void add(Model item) {
        if (item != null) {
            models.add(item);
            notifyItemInserted(models.size());
        }
    }

    public void addInverse(Model item) {
        if (item != null) {
            models.add(0, item);  // shifting insertion
            notifyItemInserted(0);
        }
    }

    public void populate(List<Model> items, boolean isThereMore) {
        isInError = false;
        if (items != null && !items.isEmpty()) {
            this.models.addAll(items);
            this.isThereMore = isThereMore;
            notifyDataSetChanged();
        }
    }

    public void populateInverse(List<Model> items, boolean isThereMore) {
        Collections.reverse(items);
        populate(items, isThereMore);
    }

    public void clear() {
        isInError = false;
        if (models != null && !models.isEmpty()) {
            models.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * This method silently clears internal list of items. The following scenario is quite possible:
     *
     * onActivityResult() finishes before onStart() and issues a request for data in repository,
     * which could also had finished before onStart() will happen. On low-memory old devices such
     * onActivityResult() could always follows complete destruction-creation cycle and then in onStart()
     * a stored state of some Presenter could be restored, populating Adapter with some preserved data.
     * But there is data already in Adapter, so it will be duplicated. To avoid this - such silent
     * cleaning should be made in such scenario.
     *
     * This method should not be called in load-more scenario.
     */
    public void clearSilent() {
        isInError = false;
        models.clear();
    }

    public void remove(int position) {
        if (models.size() > position && position >= 0) {
            models.remove(position);
            notifyItemRemoved(position);
        }
    }

    /* Customization */
    // --------------------------------------------------------------------------------------------
    protected BaseViewHolder createLoadingViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_loading, parent, false);
        return new LoadingViewHolder(view);
    }

    protected BaseViewHolder createErrorViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_error, parent, false);
        return new ErrorViewHolder(view, onErrorClickListener);
    }

    protected abstract ModelViewHolder createModelViewHolder(ViewGroup parent);
}
