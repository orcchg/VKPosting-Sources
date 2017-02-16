package com.orcchg.vikstra.app.ui.settings.group.model;

import com.google.gson.annotations.SerializedName;
import com.orcchg.vikstra.app.ui.settings.BaseSetting;

public class GroupSelector extends BaseSetting {
    public static final String TAG = "SETTING_GROUP_SELECTOR";

    @SerializedName("allSelected") boolean allSelected;

    @Override
    public void copy(BaseSetting object) {
        GroupSelector model = (GroupSelector) object;
        this.allSelected = model.allSelected;
    }

    @Override
    public String getTag() {
        return TAG;
    }
}
