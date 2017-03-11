package com.orcchg.vikstra.app.ui.group.custom.fragment.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.group.custom.fragment.GroupCustomListFragment;
import com.orcchg.vikstra.app.ui.group.custom.fragment.GroupCustomListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupCustomListModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupCustomListComponent {

    void inject(GroupCustomListFragment fragment);

    GroupCustomListPresenter presenter();
}
