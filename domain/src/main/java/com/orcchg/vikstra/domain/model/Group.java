package com.orcchg.vikstra.domain.model;

import android.support.annotation.NonNull;

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
        public abstract Builder setMembersCount(int count);
        public abstract Builder setName(String name);
        public abstract Group build();
    }

    public abstract @External long id();  // id of group in Social Network
    public abstract boolean canPost();
    public abstract int membersCount();
    public abstract String name();

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
