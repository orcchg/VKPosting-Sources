package com.orcchg.vikstra.app.ui.group.custom.activity.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.group.custom.activity.GroupCustomListActivity;
import com.orcchg.vikstra.app.ui.group.custom.activity.GroupCustomListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupCustomListModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupCustomListComponent {

    void inject(GroupCustomListActivity activity);

    GroupCustomListPresenter presenter();
}
