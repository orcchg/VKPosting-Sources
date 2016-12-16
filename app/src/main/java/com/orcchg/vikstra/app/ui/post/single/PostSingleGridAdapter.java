package com.orcchg.vikstra.app.ui.post.single;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.BaseViewHolder;
import com.orcchg.vikstra.app.ui.post.single.viewholder.NewPostSingleGridViewHolder;
import com.orcchg.vikstra.app.ui.post.single.viewholder.PostSingleGridViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.util.Constant;

public class PostSingleGridAdapter extends BaseAdapter<PostSingleGridViewHolder, PostSingleGridItemVO> {

    protected static final int VIEW_TYPE_ADD_NEW = 3;

    private BaseAdapter.OnItemClickListener<PostSingleGridItemVO> wrappedItemClickListener;
    private OnItemClickListener<Object> onNewItemClickListener;

    public PostSingleGridAdapter() {
        addFirstSystemItem();
        this.wrappedItemClickListener = createWrappedClickListener();
    }

    public void setOnNewItemClickListener(OnItemClickListener<Object> onNewItemClickListener) {
        this.onNewItemClickListener = onNewItemClickListener;
    }

    @Override
    protected PostSingleGridViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_post_single_grid_item, parent, false);
        PostSingleGridViewHolder viewHolder = new PostSingleGridViewHolder(view);
        viewHolder.setOnItemClickListener(wrappedItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        return viewHolder;
    }

    protected NewPostSingleGridViewHolder createAddNewViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_new_post_single_grid_item, parent, false);
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
    public int getItemViewType(int position) {
        if (position == 0) return VIEW_TYPE_ADD_NEW;
        return super.getItemViewType(position);
    }

    /* Data access */
    // --------------------------------------------------------------------------------------------
    @Override
    public void clear() {
        super.clear();
        addFirstSystemItem();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void addFirstSystemItem() {
        models.add(PostSingleGridItemVO.builder()
                .setId(Constant.BAD_ID)
                .setMediaCount(0)
                .build());
    }

    private BaseAdapter.OnItemClickListener<PostSingleGridItemVO> createWrappedClickListener() {
        return (view, viewObject, position) -> {
            for (PostSingleGridItemVO model : models) {
                if (model.id() != viewObject.id()) {
                    model.setSelection(false);
                }
            }
            notifyDataSetChanged();
            if (onItemClickListener != null) onItemClickListener.onItemClick(view, viewObject, position);
        };
    }
}
