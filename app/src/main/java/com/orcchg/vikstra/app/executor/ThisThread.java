package com.orcchg.vikstra.app.executor;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;

import javax.inject.Inject;

public class ThisThread implements PostExecuteScheduler {

    @Inject
    ThisThread() {
    }

    @Override
    public void post(Runnable command) {
        command.run();
    }
}
