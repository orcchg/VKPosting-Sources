package com.orcchg.vikstra.data.injection.repository;

import com.orcchg.vikstra.data.source.repository.group.GroupRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.keyword.KeywordRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.post.PostRepositoryImpl;
import com.orcchg.vikstra.data.source.repository.report.ReportRepositoryImpl;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepositoryModule {

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
