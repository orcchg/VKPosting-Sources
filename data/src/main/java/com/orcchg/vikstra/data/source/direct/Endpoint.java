package com.orcchg.vikstra.data.source.direct;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;

public abstract class Endpoint {

    protected final ThreadExecutor threadExecutor;
    protected final PostExecuteScheduler postExecuteScheduler;

    protected Endpoint(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        this.threadExecutor = threadExecutor;
        this.postExecuteScheduler = postExecuteScheduler;
    }
}
