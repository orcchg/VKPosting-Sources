package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.ui.group.list.GroupListMediator;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupListMediatorModule {

    private static GroupListMediator sGroupListMediator;

    @Provides
    GroupListMediator provideGroupListMediator() {
        if (sGroupListMediator == null) {
            sGroupListMediator = new GroupListMediator();
        }
        return sGroupListMediator;
    }
}
