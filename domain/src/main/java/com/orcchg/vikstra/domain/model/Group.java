package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Group implements Comparable<Group> {

    public static Builder builder() {
        return new AutoValue_Group.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setMembersCount(int count);
        public abstract Builder setName(String name);
        public abstract Group build();
    }

    public abstract long id();
    public abstract int membersCount();
    public abstract String name();

    @Override
    public int compareTo(Group o) {
        return o.membersCount() - membersCount();
    }
}
