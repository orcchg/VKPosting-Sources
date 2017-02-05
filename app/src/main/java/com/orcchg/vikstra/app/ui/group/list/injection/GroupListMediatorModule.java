package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.group.list.GroupListMediator;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupListMediatorModule {

    // TODO: solve singleton problem
    private static GroupListMediator sGroupListMediator;

    /**
     * Per-Activity singleton.
     *
     * {@see https://blog.mindorks.com/android-dagger2-critical-things-to-know-before-you-implement-275663aecc3e#.ftjg5teyi}
     */
    @Provides @PerActivity
    GroupListMediator provideGroupListMediator() {
        if (sGroupListMediator == null) {
            sGroupListMediator = new GroupListMediator();
        }
        return sGroupListMediator;
    }
}
