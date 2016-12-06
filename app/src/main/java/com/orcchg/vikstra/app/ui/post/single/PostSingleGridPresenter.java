package com.orcchg.vikstra.app.ui.post.single;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.widget.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class PostSingleGridPresenter extends BaseListPresenter<PostSingleGridContract.View>
        implements PostSingleGridContract.Presenter {

    @Inject
    PostSingleGridPresenter() {
        this.listAdapter = createListAdapter();
    }

    @Override
    protected BaseAdapter createListAdapter() {
        PostSingleGridAdapter adapter = new PostSingleGridAdapter();
        adapter.setOnItemClickListener((view, keywordListItemVO, position) -> {
            if (isViewAttached()) getView().openPostViewScreen();
        });
        adapter.setOnNewItemClickListener((view, keywordListItemVO, position) -> {
            if (isViewAttached()) getView().openNewPostScreen();
        });
        return adapter;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onStart() {
        super.onStart();
        start();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog
    public void start() {
        // TODO: fill properly with posts thumbs
        List<PostSingleGridItemVO> items = new ArrayList<>();
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        items.add(PostSingleGridItemVO.builder().build());
        listAdapter.populate(items, false);
    }
}
