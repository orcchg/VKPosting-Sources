package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.Collection;
import java.util.Iterator;

@AutoValue
public abstract class KeywordBundle implements Comparable<KeywordBundle>, Iterable<Keyword> {

    /**
     * Setting of this field must not break correspondence between this object's id
     * and id of the destination {@link GroupBundle} instance. This field is
     * equal to {@link Constant#BAD_ID} until a new search of groups by keywords
     * finishes - then this field is set to newly created (or updated) {@link GroupBundle}.
     *
     * Any attempt to reassign this field if it has already been set to some
     * valid {@link GroupBundle}'s id will cause an {@link IllegalStateException}.
     */
    private long groupBundleId = Constant.BAD_ID;

    public static Builder builder() {
        return new AutoValue_KeywordBundle.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setKeywords(Collection<Keyword> keywords);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setTitle(String title);
        public abstract KeywordBundle build();
    }

    public abstract long id();
    public abstract long timestamp();
    public abstract String title();
    public abstract Collection<Keyword> keywords();

    public long getGroupBundleId() {
        return groupBundleId;
    }
    public void setGroupBundleId(long groupBundleId) {
        if (this.groupBundleId != Constant.BAD_ID && groupBundleId != Constant.BAD_ID) {
            String message = "Attemp to reassign already existing group-bundle id! " +
                    "This breaks the correspondence between this keyword-bundle and " +
                    "some destination group-bundle, which is illegal.";
            throw new IllegalStateException(message);
        }
        this.groupBundleId = groupBundleId;
    }

    @Override
    public int compareTo(KeywordBundle o) {
        return (int) (o.timestamp() - timestamp());
    }

    @Override
    public Iterator<Keyword> iterator() {
        return keywords().iterator();
    }
}
