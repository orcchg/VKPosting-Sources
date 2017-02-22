package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

/**
 * Code 14:  Captcha
 */
public class Api14VkUseCaseException extends VkUseCaseException {
    public static final int ERROR_CODE = 14;

    public Api14VkUseCaseException(VKError error) {
        super(error);
    }
}
