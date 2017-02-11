package com.orcchg.vikstra.data.injection.direct;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkAttachLocalCache;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.post.PostPost;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DirectSourceModule {

    @Provides @Singleton
    ImageLoader provideImageLoader(RequestManager glide, ThreadExecutor executor, PostExecuteScheduler scheduler) {
        return new ImageLoader(glide, executor, scheduler);
    }

    @Provides @Singleton
    VkontakteEndpoint provideVkontakteEndpoint(PostPost postPostUseCase, ImageLoader imageLoader,
           VkAttachLocalCache attachLocalCache, ThreadExecutor executor, PostExecuteScheduler scheduler) {
        return new VkontakteEndpoint(postPostUseCase, imageLoader, attachLocalCache, executor, scheduler);
    }
}
