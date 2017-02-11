package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

public class NoConnectionException extends VkUseCaseException {

    public NoConnectionException(VKError error) {
        super(error);
    }
}
