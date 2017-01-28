package com.orcchg.vikstra.domain.interactor.common;

import com.orcchg.vikstra.domain.interactor.base.IParameters;

public class IdParameters implements IParameters {
    private final long id;

    public IdParameters(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }
}
