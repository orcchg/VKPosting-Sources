package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiCommunityArray;

import javax.inject.Inject;

public class GetGroupById extends VkUseCase<VKApiCommunityArray> {

    private long vkGroupId;

    @Inject
    public GetGroupById(long vkGroupId, ThreadExecutor threadExecutor,
                        PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.vkGroupId = vkGroupId;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        VKParameters params = VKParameters.from("group_id", vkGroupId, VKApiConst.EXTENDED, 1);
        return VKApi.groups().getById(params);
    }

    @Override
    protected VKApiCommunityArray parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKApiCommunityArray.class);
        return (VKApiCommunityArray) vkResponse.parsedModel;
    }
}
