package com.orcchg.vikstra.domain.model.misc;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.GroupReport;

@AutoValue
public abstract class PostingUnit implements Parcelable {

    public static Builder builder() {
        return new AutoValue_PostingUnit.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setCancelCount(int cancelCount);
        public abstract Builder setFailureCount(int failureCount);
        public abstract Builder setSuccessCount(int successCount);
        public abstract Builder setTotalCount(int totalCount);
        public abstract Builder setGroupReport(GroupReport groupReport);
        public abstract PostingUnit build();
    }

    public abstract int cancelCount();
    public abstract int failureCount();
    public abstract int successCount();
    public abstract int totalCount();
    public abstract GroupReport groupReport();
}
