package com.orcchg.vikstra.domain.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.util.Iterator;
import java.util.List;

@AutoValue
public abstract class GroupReportBundle implements Comparable<GroupReportBundle>, Iterable<GroupReport> {

    public static Builder builder() {
        return new AutoValue_GroupReportBundle.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(long id);
        public abstract Builder setGroupReports(List<GroupReport> reports);
        public abstract Builder setTimestamp(long ts);
        public abstract GroupReportBundle build();
    }

    public abstract long id();
    public abstract List<GroupReport> groupReports();
    public abstract long timestamp();

    @Override
    public int compareTo(@NonNull GroupReportBundle o) {
        return (int) (o.timestamp() - timestamp());
    }

    @Override
    public Iterator<GroupReport> iterator() {
        return null;
    }
}
