package com.orcchg.vikstra.app.ui.keyword.list;

import com.orcchg.vikstra.app.ui.common.screen.SimpleCollectionFragment;
import com.orcchg.vikstra.domain.util.Constant;

public class KeywordListFragment extends SimpleCollectionFragment implements KeywordListContract.SubView {
    public static final int RV_TAG = Constant.ListTag.KEYWORD_LIST_SCREEN;

    public static KeywordListFragment newInstance() {
        return new KeywordListFragment();
    }

    @Override
    protected boolean isGrid() {
        return false;
    }

    /* Contract */
    // ------------------------------------------
    @Override
    public void showKeywords(boolean isEmpty) {
        showContent(RV_TAG, isEmpty);
    }
}
