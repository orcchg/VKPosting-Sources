package com.orcchg.vikstra.domain.util.endpoint;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

public class EndpointUtility {

    public static final long BAD_VK_USER_ID = 0;

    public static long getCurrentUserId() {
        // same logic, as {@link VKSdk#isLoggedIn()}, but we need access_token fields here
        VKAccessToken accessToken = VKAccessToken.currentToken();
        return accessToken != null && !accessToken.isExpired()
                ? Long.parseLong(accessToken.userId)
                : BAD_VK_USER_ID;
    }

    public static boolean hasAccessTokenExhausted(@Nullable Throwable reason) {
        return !VKSdk.isLoggedIn() || Api5VkUseCaseException.class.isInstance(reason);
    }
}
