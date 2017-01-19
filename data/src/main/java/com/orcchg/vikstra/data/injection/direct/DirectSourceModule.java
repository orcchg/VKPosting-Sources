package com.orcchg.vikstra.data.injection.direct;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkAttachLocalCache;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;

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
    VkontakteEndpoint provideVkontakteEndpoint(ImageLoader imageLoader, VkAttachLocalCache attachLocalCache,
           ThreadExecutor executor, PostExecuteScheduler scheduler) {
        return new VkontakteEndpoint(imageLoader, attachLocalCache, executor, scheduler);
    }
}
