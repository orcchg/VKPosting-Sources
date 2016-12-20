package com.orcchg.vikstra.domain.util.vkontakte;

import com.vk.sdk.VKAccessToken;

public class VkUtility {

    public static long getCurrentUserId() {
        VKAccessToken accessToken = VKAccessToken.currentToken();
        return accessToken != null ? Long.parseLong(accessToken.userId) : 0;
    }
}
