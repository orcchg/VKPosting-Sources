package com.orcchg.vikstra.app.ui.report.history.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.report.history.ReportHistoryFragment;
import com.orcchg.vikstra.app.ui.report.history.ReportHistoryPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {ReportHistoryModule.class}, dependencies = {ApplicationComponent.class})
public interface ReportHistoryComponent {

    void inject(ReportHistoryFragment fragment);

    ReportHistoryPresenter presenter();
}
