package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.GroupReport;

@AutoValue
public abstract class ReportListItemVO {

    public static Builder builder() {
        return new AutoValue_ReportListItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setGroupId(long groupId);
        public abstract Builder setGroupName(String groupName);
        public abstract Builder setMembersCount(int count);
        public abstract Builder setReportStatus(@GroupReport.Status int status);
        public abstract ReportListItemVO build();
    }

    public abstract long groupId();
    public abstract String groupName();
    public abstract int membersCount();
    public abstract @GroupReport.Status int reportStatus();
}
