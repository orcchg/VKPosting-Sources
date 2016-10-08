package com.orcchg.vikstra.app.ui.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.list.ListFragment;
import com.orcchg.vikstra.app.ui.list.ListPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {ListModule.class}, dependencies = {ApplicationComponent.class})
public interface ListComponent {

    /**
     * A member-injection method.
     *
     * Injects all fields marked with {@link javax.inject.Inject} annotation
     * into {@link ListFragment} specified by {@param activity} parameter.
     *
     * @param fragment where to inject fields
     */
    void inject(ListFragment fragment);

    /**
     * Template for dagger-generated factory method to provide
     * an instance of {@link ListPresenter} class for where this
     * {@link ListComponent} injects to.
     */
    ListPresenter presenter();
}
