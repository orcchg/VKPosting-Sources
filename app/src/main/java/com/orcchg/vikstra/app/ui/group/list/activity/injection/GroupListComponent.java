package com.orcchg.vikstra.app.ui.group.list.activity.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListActivity;
import com.orcchg.vikstra.app.ui.group.list.activity.GroupListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupListModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupListComponent {

    void inject(GroupListActivity activity);

    GroupListPresenter presenter();
}
