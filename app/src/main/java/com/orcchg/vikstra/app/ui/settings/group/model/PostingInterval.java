package com.orcchg.vikstra.app.ui.settings.group.model;

import com.google.gson.annotations.SerializedName;
import com.orcchg.vikstra.app.ui.settings.BaseSetting;

public class PostingInterval extends BaseSetting {
    public static final String TAG = "SETTING_POSTING_INTERVAL";

    @SerializedName("interval") long interval;

    @Override
    public void copy(BaseSetting object) {
        PostingInterval model = (PostingInterval) object;
        this.interval = model.interval;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
