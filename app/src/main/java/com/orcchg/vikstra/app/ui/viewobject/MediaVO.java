package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MediaVO {

    public static Builder builder() {
        return new AutoValue_MediaVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setUrl(String url);
        public abstract MediaVO build();
    }

    public abstract String url();
}
