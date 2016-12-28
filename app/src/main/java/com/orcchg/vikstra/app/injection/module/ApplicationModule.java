package com.orcchg.vikstra.app.injection.module;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.executor.UIThread;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkAttachLocalCache;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.local.group.GroupDatabase;
import com.orcchg.vikstra.data.source.local.keyword.KeywordDatabase;
import com.orcchg.vikstra.data.source.local.post.PostDatabase;
import com.orcchg.vikstra.data.source.local.report.ReportDatabase;
import com.orcchg.vikstra.data.source.remote.group.GroupCloud;
import com.orcchg.vikstra.data.source.remote.keyword.KeywordCloud;
import com.orcchg.vikstra.data.source.remote.post.PostCloud;
import com.orcchg.vikstra.data.source.remote.report.ReportCloud;
import com.orcchg.vikstra.data.source.repository.group.GroupRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.keyword.KeywordRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.post.PostRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;
import com.orcchg.vikstra.data.source.repository.report.ReportRepositoryImpl;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.executor.UseCaseExecutor;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides objects which will live during the application lifecycle.
 */
@Module
public class ApplicationModule {

    private final AndroidApplication application;

    public ApplicationModule(AndroidApplication application) {
        this.application = application;
    }

    /* Context */
    // ------------------------------------------
    @Provides @Singleton
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides @Singleton
    RequestManager provideGlide() {
        return Glide.with(application.getApplicationContext());
    }

    /* Thread pool */
    // ------------------------------------------
    @Provides @Singleton
    ThreadExecutor provideThreadExecutor(UseCaseExecutor executor) {
        return executor;
    }

    @Provides @Singleton
    PostExecuteScheduler providePostExecuteScheduler(UIThread uiThread) {
        return uiThread;
    }

    /* Remote & Local data source */
    // ------------------------------------------
    @Provides @Singleton @Named("groupCloud")
    IGroupStorage provideCloudGroupSource(GroupCloud source) {
        return source;
    }

    @Provides @Singleton @Named("groupDatabase")
    IGroupStorage provideLocalGroupSource(GroupDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("keywordCloud")
    IKeywordStorage provideCloudKeywordSource(KeywordCloud source) {
        return source;
    }

    @Provides @Singleton @Named("keywordDatabase")
    IKeywordStorage provideLocalKeywordSource(KeywordDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("postCloud")
    IPostStorage provideCloudPostSource(PostCloud source) {
        return source;
    }

    @Provides @Singleton @Named("postDatabase")
    IPostStorage provideLocalPostSource(PostDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("reportCloud")
    IReportStorage provideCloudReportSource(ReportCloud source) {
        return source;
    }

    @Provides @Singleton @Named("reportDatabase")
    IReportStorage provideLocalReportSource(ReportDatabase source) {
        return source;
    }

    @Provides @Singleton
    ImageLoader provideImageLoader(RequestManager glide, ThreadExecutor executor,
                                   PostExecuteScheduler scheduler) {
        return new ImageLoader(glide, executor, scheduler);
    }

    @Provides @Singleton
    VkontakteEndpoint provideVkontakteEndpoint(ImageLoader imageLoader, VkAttachLocalCache attachLocalCache,
                                               ThreadExecutor executor, PostExecuteScheduler scheduler) {
        return new VkontakteEndpoint(imageLoader, attachLocalCache, executor, scheduler);
    }

    /* Repository */
    // ------------------------------------------
    @Provides @Singleton
    IGroupRepository provideGroupRepository(GroupRepositoryImpl repository) {
        return repository;
    }

    @Provides @Singleton
    IKeywordRepository provideKeywordRepository(KeywordRepositoryImpl repository) {
        return repository;
    }

    @Provides @Singleton
    IPostRepository providePostRepository(PostRepositoryImpl repository) {
        return repository;
    }

    @Provides @Singleton
    IReportRepository provideReportRepository(ReportRepositoryImpl repository) {
        return repository;
    }
}
