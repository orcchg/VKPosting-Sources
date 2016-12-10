package com.orcchg.vikstra.app.ui.group.detail.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.group.detail.GroupDetailPresenter;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupDetailModule {

    private final long groupId;

    public GroupDetailModule(long groupId) {
        this.groupId = groupId;
    }

    @Provides @PerActivity
    public GroupDetailPresenter provideGroupDetailPresenter(VkontakteEndpoint endpoint) {
        return new GroupDetailPresenter(groupId, endpoint);
    }
}
