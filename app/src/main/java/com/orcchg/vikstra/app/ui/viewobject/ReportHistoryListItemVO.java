package com.orcchg.vikstra.app.ui.viewobject;

import com.google.auto.value.AutoValue;
import com.orcchg.vikstra.domain.model.Keyword;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@AutoValue
public abstract class ReportHistoryListItemVO {

    private int posted, total;

    public static Builder builder() {
        return new AutoValue_ReportHistoryListItemVO.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setKeywords(List<Keyword> keywords);
        public abstract Builder setPost(PostSingleGridItemVO post);
        public abstract Builder setTimestamp(long ts);
        public abstract ReportHistoryListItemVO build();
    }

    public abstract List<Keyword> keywords();
    public abstract PostSingleGridItemVO post();
    public abstract long timestamp();

    public String dateTime() {
        Date date = new Date(timestamp());
        return new SimpleDateFormat("dd MM yyyy  HH:mm", Locale.ENGLISH).format(date);
    }

    public int posted() {
        return posted;
    }

    public void setPosted(int posted) {
        this.posted = posted;
    }

    public int total() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
