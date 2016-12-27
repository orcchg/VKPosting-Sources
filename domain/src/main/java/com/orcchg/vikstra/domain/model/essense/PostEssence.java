package com.orcchg.vikstra.domain.model.essense;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Media;

import java.util.List;

@AutoValue
public abstract class PostEssence implements Essence {

    public static Builder builder() {
        return new AutoValue_PostEssence.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setDescription(String description);
        public abstract Builder setMedia(List<Media> media);
        public abstract Builder setTitle(String title);
        public abstract PostEssence build();
    }

    public abstract String description();
    public abstract String title();
    public abstract List<Media> media();
}
