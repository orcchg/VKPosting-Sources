package com.orcchg.vikstra.domain.sample.injection;

import com.orcchg.vikstra.domain.BaseTest;

//import dagger.Component;

//@Component(modules = {TestModule.class})
public interface TestComponent {

    void inject(BaseTest test);
}
