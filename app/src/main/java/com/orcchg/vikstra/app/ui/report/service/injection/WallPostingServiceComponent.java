package com.orcchg.vikstra.app.ui.report.service.injection;

import com.orcchg.vikstra.app.injection.PerService;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;

import dagger.Component;

@PerService
@Component(modules = {WallPostingServiceModule.class}, dependencies = {ApplicationComponent.class})
public interface WallPostingServiceComponent {

    PutGroupReportBundle putGroupReportBundleUseCase();
    VkontakteEndpoint vkontakteEndpoint();
}
