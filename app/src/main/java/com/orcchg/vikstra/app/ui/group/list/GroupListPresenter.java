package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private List<GroupParentItem> groupParentItems;
    private GroupListAdapter listAdapter;

    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    @Inject
    GroupListPresenter(GetKeywordBundleById getKeywordBundleByIdUseCase, VkontakteEndpoint vkontakteEndpoint) {
        this.groupParentItems = new ArrayList<>();
        this.listAdapter = createListAdapter();

        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.vkontakteEndpoint = vkontakteEndpoint;
    }

    private GroupListAdapter createListAdapter() {
        return new GroupListAdapter(groupParentItems);
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onStart() {
        if (isViewAttached()) {  // TODO: try to use BaseListPresenter
            RecyclerView list = getView().getListView();
            if (list.getAdapter() == null) {
                list.setAdapter(listAdapter);
            }
        }
        super.onStart();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void retry() {
        // TODO: re-issue groups by keywords
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                // TODO: NPE handle - crash when BAD_ID or not found keywords
                for (Keyword keyword : bundle) {
                    GroupParentItem item = new GroupParentItem(keyword.keyword());
                    groupParentItems.add(item);
                }
                vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback());
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<List<Group>>> createGetGroupsByKeywordsListCallback() {
        return new UseCase.OnPostExecuteCallback<List<List<Group>>>() {
            @Override
            public void onFinish(@Nullable List<List<Group>> splitGroups) {
                // TODO: NPE
                // TODO: only 20 groups by single keyword by default
                int index = 0;
                for (List<Group> groups : splitGroups) {
                    List<GroupChildItem> childItems = new ArrayList<>(groups.size());
                    for (Group group : groups) {
                        GroupChildItem childItem = new GroupChildItem(group);
                        childItems.add(childItem);
                    }
                    groupParentItems.get(index).setChildList(childItems);
                    ++index;
                }

                listAdapter.notifyParentDataSetChanged(false);
                if (isViewAttached()) getView().showGroups(splitGroups.isEmpty());
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }
}
