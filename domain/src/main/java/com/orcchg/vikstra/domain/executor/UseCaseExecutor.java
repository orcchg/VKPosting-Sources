package com.orcchg.vikstra.domain.executor;

import com.orcchg.vikstra.domain.interactor.base.UseCase;

import javax.inject.Inject;

public class UseCaseExecutor extends ThreadExecutor {

    @Inject
    public UseCaseExecutor() {
    }

    public <Result> void execute(UseCase<Result> useCase) {
        super.execute(useCase);
    }
}
