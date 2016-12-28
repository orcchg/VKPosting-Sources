package com.orcchg.vikstra.domain.model;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@AutoValue
public abstract class GroupReport implements Comparable<GroupReport> {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILURE = 1;
    @IntDef({STATUS_SUCCESS, STATUS_FAILURE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    public static Builder builder() {
        return new AutoValue_GroupReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroup(Group group);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setWallPostId(int id);
        public abstract GroupReport build();
    }

    public abstract long id();
    public abstract int errorCode();
    public abstract Group group();
    public abstract long timestamp();
    public abstract int wallPostId();

    @Status
    public int status() {
        if (errorCode() == 0) return STATUS_SUCCESS;
        return STATUS_FAILURE;
    }

    @Override
    public int compareTo(@NonNull GroupReport o) {
        return (int) (o.timestamp() - timestamp());
    }
}
