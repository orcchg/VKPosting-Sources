package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 5:  Access token has expired
 */
public class Api5VkUseCaseException extends VkUseCaseException {
    public static final int ERROR_CODE = 5;

    public Api5VkUseCaseException(VKError error) {
        super(error);
    }
}
