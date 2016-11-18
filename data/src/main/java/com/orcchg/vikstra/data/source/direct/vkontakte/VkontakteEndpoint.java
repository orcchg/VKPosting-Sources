package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.direct.Endpoint;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupById;
import com.orcchg.vikstra.domain.interactor.vkontakte.GetGroupsByKeywordsList;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Keyword;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class VkontakteEndpoint extends Endpoint {

    @Inject
    public VkontakteEndpoint(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    /**
     * Get group {@link Group} by it's string id {@param id}.
     */
    private Group getGroupById(String id, @Nullable final UseCase.OnPostExecuteCallback<Group> callback) {
        GetGroupById useCase = new GetGroupById(id, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<VKApiCommunityArray>() {
            @Override
            public void onFinish(VKApiCommunityArray values) {
                if (callback != null) {
                    callback.onFinish(convert(values.get(0)));
                }
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /**
     * For each keyword {@link Keyword} in list {@param keywords} retrieves a list of groups {@link Group}.
     * Because one keyword generally corresponds to multiple groups, the resulting list is merged
     * and contains all retrieved groups.
     */
    public void getGroupsByKeywords(final List<Keyword> keywords,
                                    @Nullable final UseCase.OnPostExecuteCallback<List<Group>> callback) {
        GetGroupsByKeywordsList useCase = new GetGroupsByKeywordsList(keywords, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(new UseCase.OnPostExecuteCallback<List<VKApiCommunityArray>>() {
            @Override
            public void onFinish(List<VKApiCommunityArray> values) {
                if (callback != null) {
                    callback.onFinish(convertMerge(values));
                }
            }

            @Override
            public void onError(Throwable e) {
                if (callback != null) callback.onError(e);
            }
        });
        useCase.execute();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------

    /* Convertion */
    // --------------------------------------------------------------------------------------------
    private List<Group> convertMerge(List<VKApiCommunityArray> vkModels) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            groups.addAll(convert(vkCommunityArray));
        }
        return groups;
    }

    private List<List<Group>> convertSplit(List<VKApiCommunityArray> vkModels) {
        List<List<Group>> groupsSplit = new ArrayList<>();
        for (VKApiCommunityArray vkCommunityArray : vkModels) {
            List<Group> groups = convert(vkCommunityArray);
            groupsSplit.add(groups);
        }
        return groupsSplit;
    }

    private List<Group> convert(VKApiCommunityArray vkCommunityArray) {
        List<Group> groups = new ArrayList<>();
        for (VKApiCommunityFull vkGroup : vkCommunityArray) {
            groups.add(convert(vkGroup));
        }
        return groups;
    }

    private Group convert(VKApiCommunityFull vkGroup) {
        return Group.builder().setName(vkGroup.name).build();
    }
}
