package com.orcchg.vikstra.app.ui.base.mediator;

import timber.log.Timber;

public class MediatorReAttachException extends RuntimeException {

    public MediatorReAttachException(String message) {
        super(message);
        Timber.e(message);
    }
}
