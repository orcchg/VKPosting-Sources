package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Keyword;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiCommunityArray;

import javax.inject.Inject;

public class GetGroupsByKeyword extends VkUseCase<VKApiCommunityArray> {

    private Keyword keyword;

    @Inject
    public GetGroupsByKeyword(Keyword keyword, ThreadExecutor threadExecutor,
                              PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keyword = keyword;
    }

    protected GetGroupsByKeyword(Keyword keyword) {
        super();
    }

    @Override
    protected VKRequest prepareVkRequest() {
        VKParameters params = VKParameters.from("q", keyword.keyword());
        return VKApi.groups().search(params);
    }

    @Override
    protected VKApiCommunityArray parseVkResponse() {
        return (VKApiCommunityArray) vkResponse.parsedModel;
    }
}

