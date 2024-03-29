package com.orcchg.vikstra.domain.exception.vkontakte;

import android.support.annotation.NonNull;

import com.vk.sdk.api.VKError;

import timber.log.Timber;

public class VkUseCaseExceptionFactory {

    @NonNull
    public static VkUseCaseException create(@NonNull VKError error) {
        Timber.e("Error: code [%s], message [%s], reason [%s]",
                error.errorCode, error.errorMessage, error.errorReason);
        if (error.apiError != null) {
            Timber.e("API error: %s", error.apiError.toString());
            switch (error.apiError.errorCode) {
                case 5:   return new Api5VkUseCaseException(error);
                case 6:   return new Api6VkUseCaseException(error);
                case 14:  return new Api14VkUseCaseException(error);
                case 210: return new Api210VkUseCaseException(error);
                case 214: return new Api214VkUseCaseException(error);
                case 220: return new Api220VkUseCaseException(error);
            }
        }
        switch (error.errorCode) {
            case VKError.VK_REQUEST_HTTP_FAILED:
                return new NoConnectionException(error);
        }
        return new VkUseCaseException(error);
    }

    public static int errorCode(Throwable reason) {
        if (VkUseCaseException.class.isInstance(reason)) {
            return ((VkUseCaseException) reason).getErrorCode();
        }
        return 0;
    }
}
