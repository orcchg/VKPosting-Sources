package com.orcchg.vikstra.domain.executor;

public interface PostExecuteScheduler {
    void post(Runnable command);
}
