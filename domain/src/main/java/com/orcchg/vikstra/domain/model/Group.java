package com.orcchg.vikstra.domain.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.util.External;

@AutoValue
public abstract class Group implements Comparable<Group> {

    private boolean isSelected;

    public static Builder builder() {
        return new AutoValue_Group.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setCanPost(boolean canPost);
        public abstract Builder setKeyword(Keyword keyword);
        public abstract Builder setLink(String link);
        public abstract Builder setMembersCount(int count);
        public abstract Builder setName(String name);
        public abstract Builder setScreenName(String screenName);
        public abstract Builder setWebSite(String webSite);
        public abstract Group build();
    }

    public abstract @External long id();  // id of group in Social Network
    public abstract boolean canPost();
    public abstract @Nullable Keyword keyword();
    public abstract String link();
    public abstract int membersCount();
    public abstract String name();
    public abstract String screenName();
    public abstract String webSite();

    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int compareTo(@NonNull Group o) {
        return o.membersCount() - membersCount();
    }
}
