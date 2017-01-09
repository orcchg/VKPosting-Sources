package com.orcchg.vikstra.domain.interactor.vkontakte.batch;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.vkontakte.media.UploadPhotos;
import com.orcchg.vikstra.domain.util.vkontakte.VkUtility;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UploadPhotosBatch extends VkBatchUseCase<VKPhotoArray, List<VKPhotoArray>> {

    UploadPhotos.Parameters parameters;

    @Inject
    public UploadPhotosBatch(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected UploadPhotosBatch() {
        super();
    }

    public void setParameters(UploadPhotos.Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected VKBatchRequest prepareVkBatchRequest() {
        if (parameters == null) throw new NoParametersException();
        int size = parameters.getBitmaps().size();
        VKRequest[] requests = new VKRequest[size];
        for (int i = 0; i < size; ++i) {
            VKUploadImage image = new VKUploadImage(parameters.getBitmaps().get(i), VKImageParameters.jpgImage(0.9f));
            requests[i] = VKApi.uploadWallPhotoRequest(image, VkUtility.getCurrentUserId(), 0);
        }
        return new VKBatchRequest(requests);
    }

    @Override
    protected List<VKPhotoArray> parseVkBatchResponse() {
        List<VKPhotoArray> list = new ArrayList<>();
        for (int i = 0; i < vkBatchResponse.length; ++i) {
            VKPhotoArray data = (VKPhotoArray) vkBatchResponse[i].parsedModel;
            list.add(data);
        }
        return list;
    }
}
