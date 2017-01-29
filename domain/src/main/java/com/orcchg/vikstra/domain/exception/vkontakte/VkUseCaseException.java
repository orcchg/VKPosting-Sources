package com.orcchg.vikstra.domain.exception.vkontakte;

import android.support.annotation.NonNull;

import com.vk.sdk.api.VKError;

import hugo.weaving.DebugLog;

public class VkUseCaseException extends RuntimeException {

    protected @NonNull VKError error;

    @DebugLog
    public VkUseCaseException(@NonNull VKError error) {
        this.error = error;
    }

    @DebugLog
    public int getErrorCode() {
        if (error.apiError != null) return error.apiError.errorCode;
        return error.errorCode;
    }

    @DebugLog @Override
    public String toString() {
        if (error.apiError != null) return error.apiError.toString();
        return error.toString();
    }
}
