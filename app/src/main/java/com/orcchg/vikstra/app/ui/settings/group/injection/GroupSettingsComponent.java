package com.orcchg.vikstra.app.ui.settings.group.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.settings.group.GroupSettingsActivity;
import com.orcchg.vikstra.app.ui.settings.group.GroupSettingsPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupSettingsModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupSettingsComponent {

    void inject(GroupSettingsActivity activity);

    GroupSettingsPresenter presenter();
}
