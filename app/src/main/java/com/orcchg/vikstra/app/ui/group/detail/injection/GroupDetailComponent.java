package com.orcchg.vikstra.app.ui.group.detail.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailActivity;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {GroupDetailModule.class}, dependencies = {ApplicationComponent.class})
public interface GroupDetailComponent {

    void inject(GroupDetailActivity activity);

    GroupDetailPresenter presenter();
}
