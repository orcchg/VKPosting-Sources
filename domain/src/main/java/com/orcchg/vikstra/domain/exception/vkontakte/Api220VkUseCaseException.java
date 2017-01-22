package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 220:  Too many recipients
 */
public class Api220VkUseCaseException extends VkUseCaseException {

    public Api220VkUseCaseException(VKError error) {
        super(error);
    }
}
