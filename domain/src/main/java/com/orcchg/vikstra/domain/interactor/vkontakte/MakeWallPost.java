package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import javax.inject.Inject;

public class MakeWallPost extends VkUseCase<VKWallPostResult> {

    private String ownerId;
    private VKAttachments attachments;
    private String message;

    @Inject
    MakeWallPost(String ownerId, VKAttachments attachments, String message,
                 ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.ownerId = ownerId;
        this.attachments = attachments;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        VKParameters params = new VKParameters();
        params.put(VKApiConst.OWNER_ID, ownerId);  // destination user / community id
        params.put(VKApiConst.ATTACHMENTS, attachments);
        params.put(VKApiConst.MESSAGE, message);
        params.put(VKApiConst.EXTENDED, 1);
        return VKApi.wall().post(params);
    }

    @Override
    protected VKWallPostResult parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKWallPostResult.class);
        return (VKWallPostResult) vkResponse.parsedModel;
    }
}
