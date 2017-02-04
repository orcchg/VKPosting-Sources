package com.orcchg.vikstra.domain.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that model is too large to be stored in Bundle;
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Heavy {
}
