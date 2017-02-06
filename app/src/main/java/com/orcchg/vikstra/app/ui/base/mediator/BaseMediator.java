package com.orcchg.vikstra.app.ui.base.mediator;

import com.orcchg.vikstra.app.ui.base.MvpPresenter;

/**
 * ViewMediator provides connection between two (or more) {@link MvpPresenter} instances.
 * Concrete subclasses must be isolated within corresponding packages.
 */
public abstract class BaseMediator<FirstClient extends MediatorReceiver, SecondClient extends MediatorReceiver> {

    protected FirstClient clientFirst;
    protected SecondClient clientSecond;

    public void attachFirst(FirstClient clientFirst) {
        if (this.clientFirst != null && this.clientFirst != clientFirst) {
            String message = "Attempt to re-attach first client replacing an already existing one!";
//            throw new MediatorReAttachException(message);
        }
        this.clientFirst = clientFirst;
    }

    public void attachSecond(SecondClient clientSecond) {
        if (this.clientSecond != null && this.clientSecond != clientSecond) {
            String message = "Attempt to re-attach second client replacing an already existing one!";
//            throw new MediatorReAttachException(message);
        }
        this.clientSecond = clientSecond;
    }

    public void detachFirst() {
        clientFirst = null;
    }

    public void detachSecond() {
        clientSecond = null;
    }
}
