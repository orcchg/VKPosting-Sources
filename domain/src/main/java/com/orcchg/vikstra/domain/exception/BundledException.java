package com.orcchg.vikstra.domain.exception;

import java.util.ArrayList;
import java.util.List;

public class BundledException extends RuntimeException {

    private List<Throwable> errors;

    public BundledException() {
        errors = new ArrayList<>();
    }

    public BundledException(Builder builder) {
        errors = builder.errors;
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    public void addErrors(List<Throwable> errors) {
        this.errors.addAll(errors);
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public static class Builder {
        private List<Throwable> errors = new ArrayList<>();

        public Builder addError(Throwable error) {
            errors.add(error);
            return this;
        }

        public Builder addErrors(List<Throwable> errors) {
            this.errors.addAll(errors);
            return this;
        }

        public BundledException build() {
            return new BundledException(this);
        }
    }
}
