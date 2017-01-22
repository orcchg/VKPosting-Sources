package com.orcchg.vikstra.domain.exception.vkontakte;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.vk.sdk.api.VKError;

public class VkUseCaseExceptionFactory {

    @Nullable
    public static VkUseCaseException create(@NonNull VKError error) {
        switch (error.apiError.errorCode) {
            case 214: return new Api214VkUseCaseException(error);
            case 220: return new Api220VkUseCaseException(error);
        }
        return null;
    }
}
