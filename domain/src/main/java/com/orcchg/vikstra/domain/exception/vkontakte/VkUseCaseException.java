package com.orcchg.vikstra.domain.exception.vkontakte;

import com.vk.sdk.api.VKError;

public class VkUseCaseException extends RuntimeException {

    protected VKError error;

    public VkUseCaseException(VKError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return error.apiError.toString();
    }

    public int getErrorCode() {
        return error.apiError.errorCode;
    }
}
