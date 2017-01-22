package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 214:  Access to adding post denied: access to the wall is closed
 */
public class Api214VkUseCaseException extends VkUseCaseException {

    public Api214VkUseCaseException(VKError error) {
        super(error);
    }
}
