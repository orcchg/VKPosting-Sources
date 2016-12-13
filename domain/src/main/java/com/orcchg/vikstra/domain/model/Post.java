package com.orcchg.vikstra.domain.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Post implements Comparable<Post> {

    public static Builder builder() {
        return new AutoValue_Post.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setDescription(String description);
        public abstract Builder setMedia(List<Media> media);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setTitle(String title);
        public abstract Post build();
    }

    public abstract long id();
    public abstract String description();
    public abstract long timestamp();
    public abstract String title();
    public abstract List<Media> media();

    @Override
    public int compareTo(Post o) {
        return (int) (o.timestamp() - timestamp());
    }
}
