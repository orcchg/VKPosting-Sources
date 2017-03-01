package com.orcchg.vikstra.domain.util.endpoint;

import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseExceptionFactory;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

/**
 * Designed to work with Vkontakte at the moment.
 */
public class EndpointUtility {

    public static final long BAD_USER_ID = 0;

    public static long getCurrentUserId() {
        // same logic, as {@link VKSdk#isLoggedIn()}, but we need access_token fields here
        VKAccessToken accessToken = VKAccessToken.currentToken();
        return accessToken != null && !accessToken.isExpired()
                ? Long.parseLong(accessToken.userId)
                : BAD_USER_ID;
    }

    public static int errorCode(Throwable reason) {
        return VkUseCaseExceptionFactory.errorCode(reason);
    }

    public static boolean hasAccessTokenExhausted() {
        return !VKSdk.isLoggedIn();
    }

    public static boolean hasAccessTokenExhausted(int apiErrorCode) {
        return !VKSdk.isLoggedIn() || apiErrorCode == Api5VkUseCaseException.ERROR_CODE;
    }
}
