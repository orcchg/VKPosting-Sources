package com.orcchg.vikstra.app.ui.base.widget;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.BaseViewHolder;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.ErrorViewHolder;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.LoadingViewHolder;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<ModelViewHolder extends NormalViewHolder<Model>, Model> extends RecyclerView.Adapter<BaseViewHolder> {
    public static final int SELECT_MODE_NONE = 0;
    public static final int SELECT_MODE_SINGLE = 1;
    public static final int SELECT_MODE_MULTI = 2;
    @IntDef({SELECT_MODE_NONE, SELECT_MODE_SINGLE, SELECT_MODE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SelectMode {}

    protected final @SelectMode int selectMode;

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

    public BaseAdapter(@SelectMode int selectMode) {
        this.selectMode = selectMode;
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

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_NORMAL:
                ((ModelViewHolder) holder).bind(models.get(position));
                break;
        }
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
            this.models.add(item);
            notifyItemInserted(this.models.size());
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

    public void clear() {
        isInError = false;
        models.clear();
        notifyDataSetChanged();
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

    /* Click item */
    // --------------------------------------------------------------------------------------------
//    private BaseAdapter.OnItemClickListener<Model> createWrappedClickListener() {
//        return (view, viewObject, position) -> {
//            switch (selectMode) {
//                case SELECT_MODE_NONE:
//                case SELECT_MODE_MULTI:
//                    // TODO: accumulate selected items
//                    break;
//                case SELECT_MODE_SINGLE:
//                    for (Model model : models) {
//                        if (model.id() != viewObject.id()) {
//                            model.setSelection(false);
//                        }
//                    }
//                    notifyDataSetChanged();
//                    break;
//            }
//            if (onItemClickListener != null) onItemClickListener.onItemClick(view, viewObject, position);
//        };
//    }
}
