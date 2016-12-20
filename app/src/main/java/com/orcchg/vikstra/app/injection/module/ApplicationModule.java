package com.orcchg.vikstra.app.injection.module;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.executor.UIThread;
import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.local.DatabaseHelper;
import com.orcchg.vikstra.data.source.local.artist.ArtistLocalSource;
import com.orcchg.vikstra.data.source.local.artist.ArtistLocalSourceImpl;
import com.orcchg.vikstra.data.source.local.genre.GenreLocalSource;
import com.orcchg.vikstra.data.source.local.genre.GenreLocalSourceImpl;
import com.orcchg.vikstra.data.source.local.keyword.KeywordDatabase;
import com.orcchg.vikstra.data.source.local.post.PostDatabase;
import com.orcchg.vikstra.data.source.remote.artist.ArtistDataSource;
import com.orcchg.vikstra.data.source.remote.artist.server.ServerArtistCloudSource;
import com.orcchg.vikstra.data.source.remote.artist.yandex.YandexCloudSource;
import com.orcchg.vikstra.data.source.remote.genre.GenreDataSource;
import com.orcchg.vikstra.data.source.remote.genre.server.ServerGenreCloudSource;
import com.orcchg.vikstra.data.source.remote.keyword.KeywordCloud;
import com.orcchg.vikstra.data.source.remote.post.PostCloud;
import com.orcchg.vikstra.data.source.repository.artist.ServerArtistRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.genre.ServerGenreRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.keyword.KeywordRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.post.PostRepositoryImpl;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.executor.UseCaseExecutor;
import com.orcchg.vikstra.domain.repository.IArtistRepository;
import com.orcchg.vikstra.domain.repository.IGenreRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;

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

    @Provides @Singleton
    ImageLoader provideImageLoader(RequestManager glide, ThreadExecutor executor,
                                   PostExecuteScheduler scheduler) {
        return new ImageLoader(glide, executor, scheduler);
    }

    @Provides @Singleton
    VkontakteEndpoint provideVkontakteEndpoint(ImageLoader imageLoader, ThreadExecutor executor,
                                               PostExecuteScheduler scheduler) {
        return new VkontakteEndpoint(imageLoader, executor, scheduler);
    }

    // TODO: remove
    @Provides @Singleton @Named("yandexCloud")
    ArtistDataSource provideYandexDataSource(YandexCloudSource dataSource) {
        return dataSource;
    }

    // TODO: remove
    @Provides @Singleton @Named("serverCloud")
    ArtistDataSource provideServerDataSource(ServerArtistCloudSource dataSource) {
        return dataSource;
    }

    // TODO: remove
    @Provides @Singleton
    GenreDataSource provideGenresDataSource(ServerGenreCloudSource dataSource) {
        return dataSource;
    }

    // TODO: remove
    @Provides @Singleton
    ArtistLocalSource provideArtistLocalSource(DatabaseHelper databaseHelper) {
        return new ArtistLocalSourceImpl(databaseHelper);
    }

    // TODO: remove
    @Provides @Singleton
    GenreLocalSource provideGenreLocalSource(DatabaseHelper databaseHelper) {
        return new GenreLocalSourceImpl(databaseHelper);
    }

    // TODO: remove
    @Provides @Singleton
    IArtistRepository provideArtistRepository(ServerArtistRepositoryImpl repository) {
        return repository;
    }

    // TODO: remove
    @Provides @Singleton
    IGenreRepository provideGenresRepository(ServerGenreRepositoryImpl repository) {
        return repository;
    }

    /* Repository */
    // ------------------------------------------
    @Provides @Singleton
    IKeywordRepository provideKeywordRepository(KeywordRepositoryImpl repository) {
        return repository;
    }

    @Provides @Singleton
    IPostRepository providePostRepository(PostRepositoryImpl repository) {
        return repository;
    }
}
