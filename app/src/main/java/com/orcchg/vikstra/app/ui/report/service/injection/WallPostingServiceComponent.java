package com.orcchg.vikstra.app.ui.report.service.injection;

import com.orcchg.vikstra.app.injection.PerService;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;

import dagger.Component;

@PerService
@Component(modules = {WallPostingServiceModule.class}, dependencies = {ApplicationComponent.class})
public interface WallPostingServiceComponent {

    GroupReportEssenceMapper groupReportEssenceMapper();
    PutGroupReportBundle putGroupReportBundleUseCase();
    VkontakteEndpoint vkontakteEndpoint();
}
