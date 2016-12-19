package com.orcchg.vikstra.app.ui.keyword.list.viewholder;

import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.base.widget.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.view.KeywordsFlowLayout;
import com.orcchg.vikstra.app.ui.common.view.TitledFlowView;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListAdapter;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordViewHolder extends NormalViewHolder<KeywordListItemVO> {

    @BindView(R.id.flow) TitledFlowView flowView;

    private final @KeywordListAdapter.SelectMode int selectMode;
    private BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener;

    public KeywordViewHolder(View view, @KeywordListAdapter.SelectMode int selectMode) {
        super(view);
        ButterKnife.bind(this, view);
        this.selectMode = selectMode;
    }

    public void setOnEditClickListener(BaseAdapter.OnItemClickListener<KeywordListItemVO> editClickListener) {
        this.editClickListener = editClickListener;
    }

    @Override
    public void bind(KeywordListItemVO viewObject) {
        boolean isSelectable = selectMode != BaseSelectAdapter.SELECT_MODE_NONE;
        String label = new StringBuilder().append(29).append('/').append(60).toString();  // TODO: use proper counters
        View.OnClickListener listener = createOnItemClickListener(viewObject, isSelectable);
        View.OnLongClickListener longListener = createOnItemLongClickListener(viewObject);
        KeywordsFlowLayout.OnKeywordItemClickListener keyListener = AppConfig.INSTANCE.shouldInterceptKeywordClickOnVH() ?
                null : createDefaultOnKeywordItemClickListener(itemView, listener);  // TODO: use different callback instead of NULL

        flowView.setKeywords(viewObject.keywords());
        flowView.setTitle(viewObject.title());
        flowView.setLabel(label);
        flowView.setSelection(isSelectable ? viewObject.getSelection() : false);
        flowView.setEditable(editClickListener != null);
        flowView.setOnKeywordItemClickListener(keyListener);
        flowView.setOnEditClickListener((view) -> {
            if (editClickListener != null) editClickListener.onItemClick(view, viewObject, getAdapterPosition());
        });

        itemView.setOnClickListener(listener);
        itemView.setOnLongClickListener(longListener);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private View.OnClickListener createOnItemClickListener(KeywordListItemVO viewObject, boolean isSelectable) {
        return (view) -> {
            if (isSelectable) {
                viewObject.setSelection(!viewObject.getSelection());
                flowView.setSelection(viewObject.getSelection());
            }
            if (listener != null) listener.onItemClick(view, viewObject, getAdapterPosition());
        };
    }

    private View.OnLongClickListener createOnItemLongClickListener(KeywordListItemVO viewObject) {
        return (view) -> {
            if (longListener != null) longListener.onItemLongClick(view, viewObject, getAdapterPosition());
            return false;
        };
    }

    private KeywordsFlowLayout.OnKeywordItemClickListener createDefaultOnKeywordItemClickListener(View view, View.OnClickListener listener) {
        return (keyword) -> listener.onClick(view);
    }
}
