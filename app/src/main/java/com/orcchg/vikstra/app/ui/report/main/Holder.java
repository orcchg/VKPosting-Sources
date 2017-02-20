package com.orcchg.vikstra.app.ui.report.main;

import javax.inject.Inject;

public class Holder {

    private final boolean isInteractiveMode;

    @Inject
    public Holder(boolean isInteractiveMode) {
        this.isInteractiveMode = isInteractiveMode;
    }

    boolean isInteractiveMode() {
        return isInteractiveMode;
    }
}
