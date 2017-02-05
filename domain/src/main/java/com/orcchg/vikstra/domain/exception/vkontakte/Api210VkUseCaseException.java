package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 210:  Access to wall post denied: no such post
 */
public class Api210VkUseCaseException extends VkUseCaseException {

    public Api210VkUseCaseException(VKError error) {
        super(error);
    }
}
