package com.orcchg.vikstra.data.injection.remote;

import com.orcchg.vikstra.data.source.remote.group.GroupCloud;
import com.orcchg.vikstra.data.source.remote.keyword.KeywordCloud;
import com.orcchg.vikstra.data.source.remote.post.PostCloud;
import com.orcchg.vikstra.data.source.remote.report.ReportCloud;
import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CloudSourceModule {

    @Provides @Singleton @Named("groupCloud")
    IGroupStorage provideCloudGroupSource(GroupCloud source) {
        return source;
    }

    @Provides @Singleton @Named("keywordCloud")
    IKeywordStorage provideCloudKeywordSource(KeywordCloud source) {
        return source;
    }

    @Provides @Singleton @Named("postCloud")
    IPostStorage provideCloudPostSource(PostCloud source) {
        return source;
    }

    @Provides @Singleton @Named("reportCloud")
    IReportStorage provideCloudReportSource(ReportCloud source) {
        return source;
    }

}
