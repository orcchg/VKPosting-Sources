package com.orcchg.vikstra.app.ui.details.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.details.DetailsActivity;
import com.orcchg.vikstra.app.ui.details.DetailsPresenter;

import dagger.Component;

@PerActivity
@Component(modules = {DetailsModule.class}, dependencies = {ApplicationComponent.class})
public interface DetailsComponent {

    /**
     * A member-injection method.
     *
     * Injects all fields marked with {@link javax.inject.Inject} annotation
     * into {@link DetailsActivity} specified by {@param activity} parameter.
     *
     * @param activity where to inject fields
     */
    void inject(DetailsActivity activity);

    /**
     * Template for dagger-generated factory method to provide
     * an instance of {@link DetailsPresenter} class for where this
     * {@link DetailsComponent} injects to.
     */
    DetailsPresenter presenter();
}
