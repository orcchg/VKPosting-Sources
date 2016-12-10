package com.orcchg.vikstra.app.ui.group.detail;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;

public class GroupDetailPresenter extends BasePresenter<GroupDetailContract.View> implements GroupDetailContract.Presenter {

    private final long groupId;
    private final VkontakteEndpoint vkontakteEndpoint;

    public GroupDetailPresenter(long groupId, VkontakteEndpoint vkontakteEndpoint) {
        this.groupId = groupId;
        this.vkontakteEndpoint = vkontakteEndpoint;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        vkontakteEndpoint.getGroupById(groupId, createGetGroupByIdCallback());
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Group> createGetGroupByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Group>() {
            @Override
            public void onFinish(@Nullable Group values) {
                // TODO: impl
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }
}
