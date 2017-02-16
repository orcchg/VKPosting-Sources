package com.orcchg.vikstra.app.ui.settings.group.model;

import com.google.gson.annotations.SerializedName;
import com.orcchg.vikstra.app.ui.settings.BaseSetting;

public class GroupFilter extends BaseSetting {
    public static final String TAG = "SETTING_GROUP_FILTER";

    @SerializedName("maxMemberCount") int maxMemberCount;
    @SerializedName("minMemberCount") int minMemberCount;

    @Override
    public void copy(BaseSetting object) {
        GroupFilter model = (GroupFilter) object;
        this.maxMemberCount = model.maxMemberCount;
        this.minMemberCount = model.minMemberCount;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
