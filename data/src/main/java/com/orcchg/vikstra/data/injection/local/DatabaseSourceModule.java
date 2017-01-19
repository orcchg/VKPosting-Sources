package com.orcchg.vikstra.data.injection.local;

import com.orcchg.vikstra.data.source.local.group.GroupDatabase;
import com.orcchg.vikstra.data.source.local.keyword.KeywordDatabase;
import com.orcchg.vikstra.data.source.local.post.PostDatabase;
import com.orcchg.vikstra.data.source.local.report.ReportDatabase;
import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseSourceModule {

    @Provides @Singleton @Named("groupDatabase")
    IGroupStorage provideLocalGroupSource(GroupDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("keywordDatabase")
    IKeywordStorage provideLocalKeywordSource(KeywordDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("postDatabase")
    IPostStorage provideLocalPostSource(PostDatabase source) {
        return source;
    }

    @Provides @Singleton @Named("reportDatabase")
    IReportStorage provideLocalReportSource(ReportDatabase source) {
        return source;
    }
}
