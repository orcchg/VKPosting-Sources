package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 220:  Too many recipients - exceeded daily limit for wall posting
 */
public class Api220VkUseCaseException extends VkUseCaseException {

    public Api220VkUseCaseException(VKError error) {
        super(error);
    }
}
