package com.orcchg.vikstra.app.ui.settings.group.model;

import com.google.gson.annotations.SerializedName;
import com.orcchg.vikstra.app.ui.settings.BaseSetting;

public class GroupLoadLimit extends BaseSetting {
    public static final String TAG = "SETTING_GROUP_LIMIT";

    @SerializedName("limit") int limit;

    @Override
    public void copy(BaseSetting object) {
        GroupLoadLimit model = (GroupLoadLimit) object;
        this.limit = model.limit;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
