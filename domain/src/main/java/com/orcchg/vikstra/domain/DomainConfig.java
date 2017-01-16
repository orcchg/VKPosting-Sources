package com.orcchg.vikstra.domain;

public enum DomainConfig {
    INSTANCE;

    // TODO: list configuration on application start

    public int multiUseCaseSleepInterval = 333;  // to avoid Captcha error, interval in ms
}
