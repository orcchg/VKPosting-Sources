package com.orcchg.vikstra.app.injection.component;

import android.content.Context;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.app.injection.module.ApplicationModule;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkAttachLocalCache;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.remote.injection.CloudComponent;
import com.orcchg.vikstra.data.source.remote.injection.CloudModule;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.repository.IArtistRepository;
import com.orcchg.vikstra.domain.repository.IGenreRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton // Constraints this component to one-per-application or unscoped bindings.
@Component(modules = {ApplicationModule.class, CloudModule.class})
public interface ApplicationComponent extends CloudComponent {

    /* Context */
    // ------------------------------------------
    Context context();
    RequestManager glide();

    /* Thread pool */
    // ------------------------------------------
    ThreadExecutor threadExecutor();
    PostExecuteScheduler postExecuteScheduler();

    /* Remote & Local data source */
    // ------------------------------------------
    VkontakteEndpoint vkontakteEndpoint();
    ImageLoader imageLoader();
    VkAttachLocalCache attachLocalCache();

    /* Repository */
    // ------------------------------------------
    IArtistRepository artistRepository();
    IGenreRepository genresRepository();
    IKeywordRepository keywordRepository();
    IPostRepository postRepository();
}
