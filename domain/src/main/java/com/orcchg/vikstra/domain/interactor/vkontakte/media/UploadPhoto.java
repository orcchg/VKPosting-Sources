package com.orcchg.vikstra.domain.interactor.vkontakte.media;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.vkontakte.VkUseCase;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import javax.inject.Inject;

public class UploadPhoto extends VkUseCase<VKPhotoArray> {

    public static class Parameters implements IParameters {
        final Bitmap bitmap;

        public Parameters(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    private Parameters parameters;

    @Inject
    public UploadPhoto(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected UploadPhoto(Bitmap bitmap) {
        parameters = new Parameters(bitmap);
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        if (parameters == null) throw new NoParametersException();
        VKUploadImage image = new VKUploadImage(parameters.bitmap, VKImageParameters.jpgImage(0.9f));
        return VKApi.uploadWallPhotoRequest(image, EndpointUtility.getCurrentUserId(), 0);
    }

    @Override
    protected VKPhotoArray parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKPhotoArray.class);
        return (VKPhotoArray) vkResponse.parsedModel;
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
