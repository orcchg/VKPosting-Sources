package com.orcchg.vikstra.app.ui.post.single;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.BaseListFragment;
import com.orcchg.vikstra.app.ui.post.single.injection.DaggerPostSingleGridComponent;
import com.orcchg.vikstra.app.ui.post.single.injection.PostSingleGridComponent;

import butterknife.ButterKnife;

public class PostSingleGridFragment extends BaseListFragment<PostSingleGridContract.View, PostSingleGridContract.Presenter>
        implements PostSingleGridContract.View {

    private PostSingleGridComponent postSingleGridComponent;

    @NonNull @Override
    protected PostSingleGridContract.Presenter createPresenter() {
        return postSingleGridComponent.presenter();
    }

    @Override
    protected void injectDependencies() {
        postSingleGridComponent = DaggerPostSingleGridComponent.builder()
                .applicationComponent(getApplicationComponent())
                .build();
        postSingleGridComponent.inject(this);
    }

    @Override
    protected LinearLayoutManager createLayoutManager() {
        int span = getResources().getInteger(R.integer.post_single_grid_span);
        return new GridLayoutManager(getActivity(), span, GridLayoutManager.HORIZONTAL, false);
    }

    public static PostSingleGridFragment newInstance() {
        return new PostSingleGridFragment();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_post_single_grid, container, false);
        ButterKnife.bind(this, rootView);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_items);

        return rootView;
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onScroll(int itemsLeftToEnd) {
        // TODO: impl
    }

    @Override
    public void openPostCreateScreen() {
        navigationComponent.navigator().openPostCreateScreen(getActivity());
    }

    @Override
    public void openPostViewScreen() {
        navigationComponent.navigator().openPostViewScreen(getActivity());
    }
}
