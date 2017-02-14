package com.orcchg.vikstra.app.ui.base.adapter;

import android.support.annotation.IntDef;

import com.orcchg.vikstra.app.ui.base.adapter.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.content.ISelectableModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BaseSelectAdapter<ModelViewHolder extends NormalViewHolder<Model>, Model extends ISelectableModel>
        extends BaseAdapter<ModelViewHolder, Model> {

    public static final int SELECT_MODE_NONE = 0;
    public static final int SELECT_MODE_SINGLE = 1;
    public static final int SELECT_MODE_MULTI = 2;
    @IntDef({SELECT_MODE_NONE, SELECT_MODE_SINGLE, SELECT_MODE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SelectMode {}

    protected final @SelectMode int selectMode;

    protected BaseAdapter.OnItemClickListener<Model> wrappedItemClickListener;

    public BaseSelectAdapter(@SelectMode int selectMode) {
        this.selectMode = selectMode;
        this.wrappedItemClickListener = createWrappedClickListener();
    }

    /* Click item */
    // --------------------------------------------------------------------------------------------
    protected BaseAdapter.OnItemClickListener<Model> createWrappedClickListener() {
        return (view, viewObject, position) -> {
            switch (selectMode) {
                case SELECT_MODE_NONE:
                    // no-op
                    break;
                case SELECT_MODE_MULTI:
                    // TODO: accumulate selected items
                    break;
                case SELECT_MODE_SINGLE:
                    for (Model model : models) {
                        if (model.id() != viewObject.id()) {
                            model.setSelection(false);
                        }
                    }
                    notifyDataSetChanged();
                    break;
            }
            if (onItemClickListener != null) onItemClickListener.onItemClick(view, viewObject, position);
        };
    }
}
