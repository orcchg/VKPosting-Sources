package com.orcchg.vikstra.domain.interactor.group;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;

import javax.inject.Inject;

public class PutGroupBundle extends UseCase<Long> {

    public static class Parameters {
        // TODO: set elements GroupBundle essense
    }

    Parameters parameters;

    @Inject
    public PutGroupBundle(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    @Nullable @Override
    protected Long doAction() {
        return null;  // TODO: impl
    }
}
