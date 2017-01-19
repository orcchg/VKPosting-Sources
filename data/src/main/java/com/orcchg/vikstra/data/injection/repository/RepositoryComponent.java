package com.orcchg.vikstra.data.injection.repository;

import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {RepositoryModule.class})
public interface RepositoryComponent {

//    IGroupRepository groupRepository();
//    IKeywordRepository keywordRepository();
//    IPostRepository postRepository();
//    IReportRepository reportRepository();
}
