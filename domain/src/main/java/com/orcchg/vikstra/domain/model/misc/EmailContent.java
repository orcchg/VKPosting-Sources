package com.orcchg.vikstra.domain.model.misc;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class EmailContent {

    public static Builder builder() {
        return new AutoValue_EmailContent.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setAttachment(Uri attachment);
        public abstract Builder setBody(String body);
        public abstract Builder setRecipients(List<String> recipients);
        public abstract Builder setSubject(String subject);
        public abstract EmailContent build();
    }

    public abstract @Nullable Uri attachment();
    public abstract @Nullable String body();
    public abstract List<String> recipients();
    public abstract String subject();
}
