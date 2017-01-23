package com.orcchg.vikstra.domain.util.vkontakte;

import com.vk.sdk.VKAccessToken;

public class VkUtility {

    public static final long BAD_VK_USER_ID = 0;

    public static long getCurrentUserId() {
        VKAccessToken accessToken = VKAccessToken.currentToken();
        return accessToken != null && !accessToken.isExpired()
                ? Long.parseLong(accessToken.userId)
                : BAD_VK_USER_ID;
    }
}
