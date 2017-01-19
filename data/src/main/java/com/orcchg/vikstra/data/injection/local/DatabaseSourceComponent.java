package com.orcchg.vikstra.data.injection.local;

import com.orcchg.vikstra.data.source.direct.ImageLoader;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.data.source.repository.group.IGroupStorage;
import com.orcchg.vikstra.data.source.repository.keyword.IKeywordStorage;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.data.source.repository.report.IReportStorage;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DatabaseSourceModule.class})
public interface DatabaseSourceComponent {

//    IGroupStorage localGroupSource();
//    IKeywordStorage localKeywordSource();
//    IPostStorage localPostSource();
//    IReportStorage localReportSource();
}
