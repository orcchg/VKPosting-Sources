package com.orcchg.vikstra.app.ui.settings;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.settings.group.model.GroupFilter;
import com.orcchg.vikstra.app.ui.settings.group.model.GroupLoadLimit;
import com.orcchg.vikstra.app.ui.settings.group.model.GroupSelector;
import com.orcchg.vikstra.app.ui.settings.group.model.PostingInterval;

public class SettingsFactory {

    @Nullable
    public static BaseSetting create(String tag) {
        switch (tag) {
            case GroupFilter.TAG:      return new GroupFilter();
            case GroupLoadLimit.TAG:   return new GroupLoadLimit();
            case GroupSelector.TAG:    return new GroupSelector();
            case PostingInterval.TAG:  return new PostingInterval();
            default:                   return null;
        }
    }
}
