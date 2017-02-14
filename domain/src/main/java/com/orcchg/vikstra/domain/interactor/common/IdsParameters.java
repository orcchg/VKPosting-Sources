package com.orcchg.vikstra.domain.interactor.common;

import com.orcchg.vikstra.domain.interactor.base.IParameters;

public class IdsParameters implements IParameters {
    public final long[] ids;

    public IdsParameters(long... ids) {
        this.ids = ids;
    }
}
