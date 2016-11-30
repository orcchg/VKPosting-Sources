package com.orcchg.vikstra.app.injection.module;

import android.content.Context;

import com.orcchg.vikstra.app.AndroidApplication;
import com.orcchg.vikstra.app.executor.UIThread;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.local.DatabaseHelper;
import com.orcchg.vikstra.data.source.local.artist.ArtistLocalSource;
import com.orcchg.vikstra.data.source.local.artist.ArtistLocalSourceImpl;
import com.orcchg.vikstra.data.source.local.genre.GenreLocalSource;
import com.orcchg.vikstra.data.source.local.genre.GenreLocalSourceImpl;
import com.orcchg.vikstra.data.source.remote.artist.ArtistDataSource;
import com.orcchg.vikstra.data.source.remote.artist.server.ServerArtistCloudSource;
import com.orcchg.vikstra.data.source.remote.artist.yandex.YandexCloudSource;
import com.orcchg.vikstra.data.source.remote.genre.GenreDataSource;
import com.orcchg.vikstra.data.source.remote.genre.server.ServerGenreCloudSource;
import com.orcchg.vikstra.data.source.repository.artist.ServerArtistRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.genre.ServerGenreRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.keyword.KeywordRepositoryImpl;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.executor.UseCaseExecutor;
import com.orcchg.vikstra.domain.repository.IArtistRepository;
import com.orcchg.vikstra.domain.repository.IGenreRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

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

    @Provides @Singleton
    Context provideApplicationContext() {
        return application.getApplicationContext();
    }

    @Provides @Singleton
    ThreadExecutor provideThreadExecutor(UseCaseExecutor executor) {
        return executor;
    }

    @Provides @Singleton
    PostExecuteScheduler providePostExecuteScheduler(UIThread uiThread) {
        return uiThread;
    }

    @Provides @Singleton
    VkontakteEndpoint provideVkontakteEndpoint(ThreadExecutor executor, PostExecuteScheduler scheduler) {
        return new VkontakteEndpoint(executor, scheduler);
    }

    @Provides @Singleton @Named("yandexCloud")
    ArtistDataSource provideYandexDataSource(YandexCloudSource dataSource) {
        return dataSource;
    }

    @Provides @Singleton @Named("serverCloud")
    ArtistDataSource provideServerDataSource(ServerArtistCloudSource dataSource) {
        return dataSource;
    }

    @Provides @Singleton
    GenreDataSource provideGenresDataSource(ServerGenreCloudSource dataSource) {
        return dataSource;
    }

    @Provides @Singleton
    ArtistLocalSource provideArtistLocalSource(DatabaseHelper databaseHelper) {
        return new ArtistLocalSourceImpl(databaseHelper);
    }

    @Provides @Singleton
    GenreLocalSource provideGenreLocalSource(DatabaseHelper databaseHelper) {
        return new GenreLocalSourceImpl(databaseHelper);
    }

    @Provides @Singleton
    IArtistRepository provideArtistRepository(ServerArtistRepositoryImpl repository) {
        return repository;
    }

    @Provides @Singleton
    IGenreRepository provideGenresRepository(ServerGenreRepositoryImpl repository) {
        return repository;
    }

    // TODO: remove
    @Provides @Singleton
    IKeywordRepository provideKeywordRepository(KeywordRepositoryImpl repository) {
        return repository;
    }
}
