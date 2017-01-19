package com.orcchg.vikstra.app.ui.group.list.activity.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.group.DumpGroups;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupListModule {

    private final String pathToDumpFile;

    public GroupListModule(String pathToDumpFile) {
        this.pathToDumpFile = pathToDumpFile;
    }

    @Provides @PerActivity
    protected DumpGroups provideDumpGroups(ReportComposer reportComposer, IGroupRepository groupRepository,
            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new DumpGroups(pathToDumpFile, reportComposer, groupRepository, threadExecutor, postExecuteScheduler);
    }
}
