package com.orcchg.vikstra.domain.interactor.vkontakte.media;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api14VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api6VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.model.VKPhotoArray;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class UploadPhotos extends MultiUseCase<VKPhotoArray, List<Ordered<VKPhotoArray>>> {

    public static class Parameters implements IParameters {
        final List<Bitmap> bitmaps;

        public Parameters(List<Bitmap> bitmaps) {
            this.bitmaps = bitmaps;
        }

        public List<Bitmap> getBitmaps() {
            return bitmaps;
        }
    }

    private Parameters parameters;

    @Inject @SuppressWarnings("unchecked")
    public UploadPhotos(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(0, threadExecutor, postExecuteScheduler);  // total count will be set later
        setAllowedErrors(Api6VkUseCaseException.class);
        setSuspendErrors(Api14VkUseCaseException.class);
        setTerminalErrors(Api5VkUseCaseException.class);
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected List<? extends UseCase<VKPhotoArray>> createUseCases() {
        if (parameters == null) throw new NoParametersException();

        total = parameters.getBitmaps().size();  // update total count
        Timber.d("Uploading images, total count: %s", total);
        List<UploadPhoto> useCases = new ArrayList<>();
        for (Bitmap bitmap : parameters.bitmaps) {
            UploadPhoto useCase = new UploadPhoto(bitmap);
            useCases.add(useCase);
        }
        return useCases;
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
