package com.orcchg.vikstra.app.ui.group.list.fragment.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.common.injection.KeywordModule;
import com.orcchg.vikstra.app.ui.common.injection.PostModule;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListFragment;
import com.orcchg.vikstra.app.ui.group.list.fragment.GroupListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupListModule.class, KeywordModule.class, PostModule.class},
           dependencies = {ApplicationComponent.class})
public interface GroupListComponent {

    void inject(GroupListFragment fragment);

    GroupListPresenter presenter();
}
