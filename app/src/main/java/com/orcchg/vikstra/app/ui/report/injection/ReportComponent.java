package com.orcchg.vikstra.app.ui.report.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.report.ReportActivity;
import com.orcchg.vikstra.app.ui.report.ReportPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {PostModule.class, ReportModule.class}, dependencies = {ApplicationComponent.class})
public interface ReportComponent {

    void inject(ReportActivity activity);

    ReportPresenter presenter();
}
