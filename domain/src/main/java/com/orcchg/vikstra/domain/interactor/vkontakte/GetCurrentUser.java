package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import javax.inject.Inject;

public class GetCurrentUser extends VkUseCase<VKList<VKApiUserFull>> {

    @Inject
    public GetCurrentUser(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    @Override
    protected VKRequest prepareVkRequest() {
        VKParameters params = VKParameters.from(VKApiConst.FIELDS, "photo_50");
        return VKApi.users().get(params);
    }

    @Override @SuppressWarnings("unchecked")
    protected VKList<VKApiUserFull> parseVkResponse() {
        return (VKList<VKApiUserFull>) vkResponse.parsedModel;
    }
}
