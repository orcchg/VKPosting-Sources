package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.group.list.GroupListMediator;

import dagger.Component;

@PerActivity
@Component(modules = {GroupListMediatorModule.class})
public interface GroupListMediatorComponent {

    void inject(com.orcchg.vikstra.app.ui.group.list.activity.GroupListPresenter presenter);
    void inject(com.orcchg.vikstra.app.ui.group.list.fragment.GroupListPresenter presenter);

    GroupListMediator mediator();
}
