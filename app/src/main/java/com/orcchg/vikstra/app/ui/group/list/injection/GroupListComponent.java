package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.group.list.GroupListFragment;
import com.orcchg.vikstra.app.ui.group.list.GroupListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupListModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupListComponent {

    void inject(GroupListFragment fragment);

    GroupListPresenter presenter();
}
