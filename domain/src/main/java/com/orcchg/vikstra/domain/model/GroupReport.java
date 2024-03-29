package com.orcchg.vikstra.domain.model;

import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@AutoValue
public abstract class GroupReport implements Comparable<GroupReport>, Parcelable {
    public static final int STATUS_CANCEL = 0;
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILURE = 2;
    public static final int STATUS_REVERT = 3;
    public static final int STATUSES_COUNT = STATUS_REVERT + 1;
    @IntDef({STATUS_CANCEL, STATUS_SUCCESS, STATUS_FAILURE, STATUS_REVERT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    private boolean wasReverted = false;

    public static Builder builder() {
        return new AutoValue_GroupReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setCancelled(boolean cancelled);
        public abstract Builder setErrorCode(int errorCode);
        public abstract Builder setGroup(Group group);
        public abstract Builder setTimestamp(long ts);
        public abstract Builder setWallPostId(long wallPostId);
        public abstract GroupReport build();
    }

    public abstract long id();
    public abstract boolean cancelled();
    public abstract int errorCode();
    public abstract Group group();
    public abstract long timestamp();
    public abstract long wallPostId();

    public boolean wasReverted() {
        return wasReverted;
    }

    public void setReverted(boolean wasReverted) {
        this.wasReverted = wasReverted;
    }

    @Status
    public int status() {
        if (wasReverted()) return STATUS_REVERT;
        if (cancelled()) return STATUS_CANCEL;
        if (errorCode() == 0) return STATUS_SUCCESS;
        return STATUS_FAILURE;
    }

    public String statusString() {
        switch (status()) {
            case STATUS_CANCEL:   return "Cancelled";
            case STATUS_SUCCESS:  return "Success";
            case STATUS_FAILURE:  return "Failure";
            case STATUS_REVERT:   return "Reverted";
        }
        return "";
    }

    @Override
    public int compareTo(@NonNull GroupReport o) {
        return (int) (o.timestamp() - timestamp());
    }
}
