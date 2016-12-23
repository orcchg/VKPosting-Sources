package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import java.util.Collection;
import java.util.Iterator;

@AutoValue
public abstract class GroupBundle implements Comparable<GroupBundle>, Iterable<Group> {

    public static Builder builder() {
        return new AutoValue_GroupBundle.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setGroups(Collection<Group> groups);
        public abstract Builder setKeywordBundleId(long keywordBundleId);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setTitle(String title);
        public abstract GroupBundle build();
    }

    public abstract long id();
    public abstract Collection<Group> groups();
    public abstract long keywordBundleId();
    public abstract long timestamp();
    public abstract String title();

    public int selectedCount() {
        int count = 0;
        Collection<Group> list = groups();
        for (Group group : list) {
            if (group.isSelected()) ++count;
        }
        return count;
    }

    @Override
    public int compareTo(GroupBundle o) {
        return (int) (o.timestamp() - timestamp());
    }

    @Override
    public Iterator<Group> iterator() {
        return null;
    }
}
