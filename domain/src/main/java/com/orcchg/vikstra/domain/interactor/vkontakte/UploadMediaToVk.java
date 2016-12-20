package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.google.gson.Gson;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Post;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import javax.inject.Inject;

public class UploadMediaToVk extends VkUseCase<VKPhotoArray> {

    public static class Parameters {
        Post post;

        public Parameters(Post post) {
            this.post = post;
        }
    }

    Parameters parameters;

    @Inject
    public UploadMediaToVk(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        if (parameters == null) throw new NoParametersException();
        // TODO: parse various Media types
        VKUploadImage image = new VKUploadImage(photo, VKImageParameters.jpgImage(0.9f));
        return VKApi.uploadWallPhotoRequest(image, userId, groupId);
    }

    @Override
    protected VKPhotoArray parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKPhotoArray.class);
        return (VKPhotoArray) vkResponse.parsedModel;
    }
}
