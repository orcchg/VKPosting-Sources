package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.common.IdParameters;
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
        VKParameters params = new VKParameters();
        params.put(VKApiConst.GROUP_ID, vkGroupId);
        params.put(VKApiConst.EXTENDED, 1);
        return VKApi.groups().getById(params);
    }

    @Override
    protected VKApiCommunityArray parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKApiCommunityArray.class);
        return (VKApiCommunityArray) vkResponse.parsedModel;
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new IdParameters(vkGroupId);
    }
}
