package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 6:  Too many requests
 */
public class Api6VkUseCaseException extends VkUseCaseException {

    public Api6VkUseCaseException(VKError error) {
        super(error);
    }
}
