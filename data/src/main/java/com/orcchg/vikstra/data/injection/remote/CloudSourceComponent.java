package com.orcchg.vikstra.data.injection.remote;

import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {CloudSourceModule.class})
public interface CloudSourceComponent {

//    IGroupStorage cloudGroupSource();
//    IKeywordStorage cloudKeywordSource();
//    IPostStorage cloudPostSource();
//    IReportStorage cloudReportSource();
}
