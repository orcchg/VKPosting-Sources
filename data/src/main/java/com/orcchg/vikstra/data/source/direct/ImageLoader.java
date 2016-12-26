package com.orcchg.vikstra.data.source.direct;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import timber.log.Timber;

public class ImageLoader extends Endpoint {

    private final RequestManager glide;
    private int width = 400;
    private int height = 400;

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

    public void loadImage(Media media, @Nullable UseCase.OnPostExecuteCallback<Bitmap> callback) {
        Timber.d("Loading single media");
        LoadPhoto useCase = new LoadPhoto(media, this, threadExecutor, postExecuteScheduler);
        useCase.setPostExecuteCallback(callback);
        useCase.execute();
    }

    public void loadImages(List<Media> media, @Nullable UseCase.OnPostExecuteCallback<List<Bitmap>> callback) {
        loadImages(media, callback, null);
    }

    public void loadImages(List<Media> media, @Nullable UseCase.OnPostExecuteCallback<List<Bitmap>> callback,
                           @Nullable MultiUseCase.ProgressCallback progressCallback) {
        if (media != null && !media.isEmpty()) {
            Timber.d("Loading media, total count: %s", media.size());
            LoadPhotos useCase = new LoadPhotos(media, this, threadExecutor, postExecuteScheduler);
            useCase.setProgressCallback(progressCallback);
            useCase.setPostExecuteCallback(callback);
            useCase.execute();
        } else {
            Timber.v("Nothing to be done with empty list of media");
        }
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
                    Thread.interrupted();  // retry request
                }
                elapsed = System.currentTimeMillis();
            }
            return null;
        }
    }

    private static class LoadPhotos extends MultiUseCase<Bitmap, List<Bitmap>> {
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
    }
}
