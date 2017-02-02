package com.orcchg.vikstra.app.ui.group.detail;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;

import hugo.weaving.DebugLog;
import timber.log.Timber;

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
        // TODO: loading
        vkontakteEndpoint.getGroupById(groupId, createGetGroupByIdCallback());
    }

    @Override
    protected void onRestoreState() {
        freshStart();  // repeat request
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Group> createGetGroupByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Group>() {
            @DebugLog @Override
            public void onFinish(@Nullable Group group) {
                Timber.i("Use-Case: succeeded to get Group by id");
                if (group == null) {
                    Timber.e("Group wasn't found by id: %s", groupId);
                    throw new ProgramException();
                }
                if (isViewAttached()) getView().onGroupLoaded(group.link());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Group by id");
                // TODO: impl
            }
        };
    }
}
