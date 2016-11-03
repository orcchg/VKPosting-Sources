package com.orcchg.vikstra.app.ui.keyword.list.viewholder;

import android.view.View;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.widget.viewholder.NormalViewHolder;
import com.orcchg.vikstra.app.ui.common.TitledFlowView;
import com.orcchg.vikstra.app.ui.viewobject.KeywordListItemVO;

import butterknife.BindView;
import butterknife.ButterKnife;

public class KeywordViewHolder extends NormalViewHolder<KeywordListItemVO> {

    @BindView(R.id.flow) TitledFlowView flowView;

    public KeywordViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    @Override
    public void bind(KeywordListItemVO viewObject) {
        flowView.setTitle(viewObject.title());
        flowView.setKeywords(viewObject.keywords());
    }
}
