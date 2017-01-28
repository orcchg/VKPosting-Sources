package com.orcchg.vikstra.domain.interactor.common;

import com.orcchg.vikstra.domain.interactor.base.IParameters;

public class ListParameters implements IParameters {
    protected int limit = -1;
    protected int offset = 0;

    protected ListParameters(Builder builder) {
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public static class Builder<GenericBuilder extends Builder> {
        int limit = -1;
        int offset = 0;

        public GenericBuilder setLimit(int limit) {
            this.limit = limit;
            return (GenericBuilder) this;
        }

        public GenericBuilder setOffset(int offset) {
            this.offset = offset;
            return (GenericBuilder) this;
        }

        public ListParameters build() {
            return new ListParameters(this);
        }
    }

    public int limit() {
        return limit;
    }

    public int offset() {
        return offset;
    }
}
