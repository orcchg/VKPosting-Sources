package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 6:  Too many requests over time
 */
public class Api6VkUseCaseException extends VkUseCaseException {
    public static final int ERROR_CODE = 6;

    public Api6VkUseCaseException(VKError error) {
        super(error);
    }
}
