package com.orcchg.vikstra.app.ui.status.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.status.StatusDialogFragment;
import com.orcchg.vikstra.app.ui.status.StatusPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {StatusModule.class}, dependencies = {ApplicationComponent.class})
public interface StatusComponent {

    void inject(StatusDialogFragment dialog);

    StatusPresenter presenter();
}
