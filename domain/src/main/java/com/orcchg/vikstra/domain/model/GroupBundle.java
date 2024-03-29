package com.orcchg.vikstra.domain.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;

@AutoValue @Heavy
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

    public List<List<Group>> splitGroupsByKeywords() {
        Map<Keyword, Integer> keywords = new TreeMap<>();
        Collection<Group> list = groups();
        List<List<Group>> splitGroups = new ArrayList<>();
        int index = 0, position;

        for (Group group : list) {
            Keyword keyword = group.keyword();
            if (keywords.containsKey(keyword)) {
                position = keywords.get(keyword);
            } else {
                keywords.put(keyword, index);
                splitGroups.add(new ArrayList<Group>());
                position = index;
                ++index;
            }
            splitGroups.get(position).add(group);
        }
        Timber.v("Total keywords: %s", keywords.size());
        for (Map.Entry<Keyword, Integer> entry : keywords.entrySet()) {
            Timber.v(entry.getKey().keyword());
        }
        return splitGroups;
    }

    @Override
    public int compareTo(@NonNull GroupBundle o) {
        return (int) (o.timestamp() - timestamp());
    }

    @Override
    public Iterator<Group> iterator() {
        return groups().iterator();
    }
}
