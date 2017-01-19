package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class GetGroupBundleById extends UseCase<GroupBundle> {

    private long id = Constant.BAD_ID;
    private final IGroupRepository groupRepository;

    @Inject
    public GetGroupBundleById(long id, IGroupRepository groupRepository,
                              ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.groupRepository = groupRepository;
    }

    /**
     * For internal use within another {@link UseCase} and synchronous calls only
     */
    GetGroupBundleById(long id, IGroupRepository groupRepository) {
        this.id = id;
        this.groupRepository = groupRepository;
    }

    public void setGroupBundleId(long id) {
        this.id = id;
    }

    public long getGroupBundleId() {
        return id;
    }

    @Nullable @Override
    protected GroupBundle doAction() {
        return groupRepository.groups(id);
    }
}
