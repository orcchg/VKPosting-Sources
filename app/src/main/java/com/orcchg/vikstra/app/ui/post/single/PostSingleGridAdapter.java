package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.orcchg.vikstra.app.ui.post.single.viewholder.NewPostSingleGridViewHolder;
import com.orcchg.vikstra.app.ui.post.single.viewholder.PostSingleGridViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import hugo.weaving.DebugLog;

public class PostSingleGridAdapter extends BaseSelectAdapter<PostSingleGridViewHolder, PostSingleGridItemVO> {

    protected static final int VIEW_TYPE_ADD_NEW = 3;

    private final boolean withAddItem;

    private BaseAdapter.OnItemClickListener<PostSingleGridItemVO> wrappedItemClickListener;
    private OnItemClickListener<Object> onNewItemClickListener;

    public PostSingleGridAdapter(@SelectMode int selectMode, boolean withAddItem) {
        super(selectMode);
        this.withAddItem = withAddItem;
        this.wrappedItemClickListener = createWrappedClickListener();
    }

    public void setOnNewItemClickListener(OnItemClickListener<Object> onNewItemClickListener) {
        this.onNewItemClickListener = onNewItemClickListener;
    }

    @Override
    protected PostSingleGridViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(VIEW_TYPE_NORMAL), parent, false);
        PostSingleGridViewHolder viewHolder = new PostSingleGridViewHolder(view);
        viewHolder.setOnItemClickListener(wrappedItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        return viewHolder;
    }

    protected NewPostSingleGridViewHolder createAddNewViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(VIEW_TYPE_ADD_NEW), parent, false);
        return new NewPostSingleGridViewHolder(view, onNewItemClickListener);
    }

    @Nullable @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        if (viewHolder == null && viewType == VIEW_TYPE_ADD_NEW) {
            viewHolder = createAddNewViewHolder(parent);
        }
        return viewHolder;
    }

    @Override
    protected PostSingleGridItemVO getItemAtPosition(int position) {
        int modelPosition = position + (withAddItem ? -1 : 0);
        return super.getItemAtPosition(modelPosition);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (withAddItem ? 1 : 0);
    }

    @LayoutRes
    public int getItemLayout(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ADD_NEW: return R.layout.rv_new_post_single_grid_item;
            case VIEW_TYPE_NORMAL:
            default:  // TODO: support loading / error view-types
                return R.layout.rv_post_single_grid_item;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (withAddItem && position == 0) return VIEW_TYPE_ADD_NEW;
        return super.getItemViewType(position);
    }

    @DebugLog
    public void selectItemAtPosition(int modelPosition, boolean isSelected) {
        PostSingleGridItemVO vo = models.get(modelPosition);
        vo.setSelection(isSelected);
        notifyItemChanged(withAddItem ? modelPosition + 1 : modelPosition);
    }

    /* Data access */
    // --------------------------------------------------------------------------------------------
    public boolean withAddItem() {
        return withAddItem;
    }
}
