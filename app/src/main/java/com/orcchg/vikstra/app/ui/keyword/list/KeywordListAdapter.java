package com.orcchg.vikstra.app.ui.keyword.list;

import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.viewholder.KeywordViewHolder;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class KeywordListAdapter extends BaseAdapter<KeywordViewHolder, KeywordListItemVO> {
    public static final int SELECT_MODE_NONE = 0;
    public static final int SELECT_MODE_SINGLE = 1;
    public static final int SELECT_MODE_MULTI = 2;
    @IntDef({SELECT_MODE_NONE, SELECT_MODE_SINGLE, SELECT_MODE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SelectMode {}

    private final @SelectMode int selectMode;
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> wrappedItemClickListener;
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener;

    public KeywordListAdapter(@SelectMode int selectMode) {
        this.selectMode = selectMode;
        this.wrappedItemClickListener = createWrappedClickListener();
    }

    public void setOnEditClickListener(BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener) {
        this.editClickListener = editClickListener;
    }

    @Override
    protected KeywordViewHolder createModelViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_keywords, parent, false);
        KeywordViewHolder viewHolder = new KeywordViewHolder(view, selectMode);
        viewHolder.setOnItemClickListener(wrappedItemClickListener);
        viewHolder.setOnItemLongClickListener(onItemLongClickListener);
        viewHolder.setOnEditClickListener(editClickListener);
        return viewHolder;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> createWrappedClickListener() {
        return (view, viewObject, position) -> {
            switch (selectMode) {
                case SELECT_MODE_NONE:
                case SELECT_MODE_MULTI:
                    // TODO: accumulate selected items
                    break;
                case SELECT_MODE_SINGLE:
                    for (KeywordListItemVO model : models) {
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
