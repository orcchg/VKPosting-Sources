package com.orcchg.vikstra.domain.interactor.vkontakte.media;

import android.graphics.Bitmap;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.model.VKPhotoArray;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class UploadPhotos extends MultiUseCase<VKPhotoArray, List<VKPhotoArray>> {

    public static class Parameters {
        List<Bitmap> bitmaps;

        public Parameters(List<Bitmap> bitmaps) {
            this.bitmaps = bitmaps;
        }
    }

    Parameters parameters;

    @Inject
    public UploadPhotos(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(0, threadExecutor, postExecuteScheduler);  // total count will be set later
//        setAllowedError();  // TODO: allow VkError with code = 6
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected List<? extends UseCase<VKPhotoArray>> createUseCases() {
        if (parameters == null) throw new NoParametersException();

        total = parameters.bitmaps.size();  // update total count
        List<UploadPhoto> useCases = new ArrayList<>();
        for (Bitmap bitmap : parameters.bitmaps) {
            UploadPhoto useCase = new UploadPhoto(bitmap);
            useCases.add(useCase);
        }
        return useCases;
    }
}
