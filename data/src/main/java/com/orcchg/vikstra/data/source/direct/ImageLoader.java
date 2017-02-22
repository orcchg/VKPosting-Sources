package com.orcchg.vikstra.data.source.direct;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ImageLoader extends Endpoint {

    private final RequestManager glide;
    private int width = 400;
    private int height = 400;

    // references to communicate with use-cases
    private LoadPhotos loadPhotosUseCase;

    @Inject
    public ImageLoader(RequestManager glide, ThreadExecutor threadExecutor,
                       PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.glide = glide;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void loadImages(List<Media> media, @Nullable UseCase.OnPostExecuteCallback<List<Ordered<Bitmap>>> callback,
                           @Nullable MultiUseCase.ProgressCallback progressCallback) {
        if (media != null && !media.isEmpty()) {
            Timber.d("Loading media, total count: %s", media.size());
            LoadPhotos useCase = new LoadPhotos(media, this, threadExecutor, postExecuteScheduler);
            useCase.setProgressCallback(progressCallback);
            useCase.setPostExecuteCallback(createLoadPhotosCallback(callback));
            loadPhotosUseCase = useCase;
            useCase.execute();
        } else {
            Timber.d("Nothing to be done with empty list of media");
        }
    }

    /* Communication */
    // ------------------------------------------
    @DebugLog
    public boolean pauseLoadPhotos() {
        Timber.d("pauseLoadPhotos");
        // communication with use-case is ignored if use-case hasn't started or has finished
        if (loadPhotosUseCase != null) loadPhotosUseCase.pause();
        return loadPhotosUseCase != null;
    }

    @DebugLog
    public boolean resumeLoadPhotos() {
        Timber.d("resumeLoadPhotos");
        // communication with use-case is ignored if use-case hasn't started or has finished
        if (loadPhotosUseCase != null) loadPhotosUseCase.resume();
        return loadPhotosUseCase != null;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<Ordered<Bitmap>>> createLoadPhotosCallback(
            UseCase.OnPostExecuteCallback<List<Ordered<Bitmap>>> callback
    ) {
        return new UseCase.OnPostExecuteCallback<List<Ordered<Bitmap>>>() {
            @Override
            public void onFinish(@Nullable List<Ordered<Bitmap>> values) {
                loadPhotosUseCase = null;  // unsubscribe from communication
                if (callback != null) callback.onFinish(values);
            }

            @Override
            public void onError(Throwable e) {
                loadPhotosUseCase = null;  // unsubscribe from communication
                if (callback != null) callback.onError(e);
            }
        };
    }

    /* Internal use-cases */
    // --------------------------------------------------------------------------------------------
    private static class LoadPhoto extends UseCase<Bitmap> {
        private final ImageLoader imageLoader;
        private final Media media;

        LoadPhoto(Media media, ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            this.media = media;
        }

        LoadPhoto(Media media, ImageLoader imageLoader, ThreadExecutor threadExecutor,
                  PostExecuteScheduler postExecuteScheduler) {
            super(threadExecutor, postExecuteScheduler);
            this.imageLoader = imageLoader;
            this.media = media;
        }

        @Nullable @Override
        protected Bitmap doAction() {
            long start = System.currentTimeMillis();
            long elapsed = start;
            while (elapsed - start < 15_000) {
                try {
                    return imageLoader.glide.load(media.url()).asBitmap().into(imageLoader.width, imageLoader.height).get();
                } catch (ExecutionException e) {
                    // retry request
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // retry request
                }
                elapsed = System.currentTimeMillis();
            }
            return null;
        }

        /* Parameters */
        // --------------------------------------
        private class Parameters implements IParameters {
            private final Media media;

            private Parameters(Media media) {
                this.media = media;
            }

            public Media media() {
                return media;
            }
        }

        @Nullable @Override
        protected IParameters getInputParameters() {
            return new Parameters(media);
        }
    }

    private static class LoadPhotos extends MultiUseCase<Bitmap, List<Ordered<Bitmap>>> {
        private final ImageLoader imageLoader;
        private final List<Media> media;

        LoadPhotos(List<Media> media, ImageLoader imageLoader, ThreadExecutor threadExecutor,
                   PostExecuteScheduler postExecuteScheduler) {
            super(media.size(), threadExecutor, postExecuteScheduler);
            this.imageLoader = imageLoader;
            this.media = media;
        }

        @Override
        protected List<? extends UseCase<Bitmap>> createUseCases() {
            List<LoadPhoto> useCases = new ArrayList<>();
            for (Media item : media) {
                LoadPhoto useCase = new LoadPhoto(item, imageLoader);
                useCases.add(useCase);
            }
            return useCases;
        }

        /* Parameters */
        // --------------------------------------
        private class Parameters implements IParameters {
            private final List<Media> media;

            private Parameters(List<Media> media) {
                this.media = media;
            }

            public List<Media> media() {
                return media;
            }
        }

        @Nullable @Override
        protected IParameters getInputParameters() {
            return new Parameters(media);
        }
    }
}
