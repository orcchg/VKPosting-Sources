package com.orcchg.vikstra.app.injection.component;

import android.content.Context;

import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.app.injection.module.ApplicationModule;
import com.orcchg.vikstra.data.injection.migration.MigrationModule;
import com.orcchg.vikstra.data.injection.remote.CloudComponent;
import com.orcchg.vikstra.data.injection.remote.CloudModule;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkAttachLocalCache;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Singleton;

import dagger.Component;

/**
 * A component whose lifetime is the life of the application.
 */
@Singleton  // Constraints this component to one-per-application or unscoped bindings.
@Component(modules = {ApplicationModule.class, CloudModule.class, MigrationModule.class})
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
    IGroupRepository groupRepository();
    IKeywordRepository keywordRepository();
    IPostRepository postRepository();
    IReportRepository reportRepository();
}
