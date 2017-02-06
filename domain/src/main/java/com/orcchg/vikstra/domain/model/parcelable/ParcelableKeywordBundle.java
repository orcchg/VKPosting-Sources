package com.orcchg.vikstra.domain.model.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ParcelableKeywordBundle implements Parcelable {

    private final KeywordBundle keywordBundle;

    public ParcelableKeywordBundle(KeywordBundle keywordBundle) {
        this.keywordBundle = keywordBundle;
    }

    private ParcelableKeywordBundle(Parcel source) {
        long id = source.readLong();
        List<Keyword> list = new ArrayList<>();
        source.readList(list, Keyword.class.getClassLoader());
        long timestamp = source.readLong();
        String title = source.readString();
        long groupBundleId = source.readLong();
        int selectedGroupsCount = source.readInt();
        int totalGroupsCount = source.readInt();

        String delim = "";
        StringBuilder listStr = new StringBuilder("[");
        for (Keyword item : list) {
            listStr.append(delim).append(item.toString());
            delim = ", ";
        }
        listStr.append(']');
        Timber.v("id=%s, list=%s, timestamp=%s, title=%s, groupBundleId=%s, selectedGroupsCount=%s, totalGroupsCount=%s",
                id, listStr.toString(), timestamp, title, groupBundleId, selectedGroupsCount, totalGroupsCount);

        keywordBundle = KeywordBundle.builder()
                .setId(id)
                .setKeywords(list)
                .setTimestamp(timestamp)
                .setTitle(title)
                .build();
        keywordBundle.setGroupBundleId(groupBundleId);
        keywordBundle.setSelectedGroupsCount(selectedGroupsCount);
        keywordBundle.setTotalGroupsCount(totalGroupsCount);
    }

    public KeywordBundle get() {
        return keywordBundle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(keywordBundle.id());
        dest.writeList(keywordBundle.keywords());
        dest.writeLong(keywordBundle.timestamp());
        dest.writeString(keywordBundle.title());
        dest.writeLong(keywordBundle.getGroupBundleId());
        dest.writeInt(keywordBundle.getSelectedGroupsCount());
        dest.writeInt(keywordBundle.getTotalGroupsCount());
    }

    public static final Parcelable.Creator<ParcelableKeywordBundle> CREATOR = new Parcelable.Creator<ParcelableKeywordBundle>() {
        @Override
        public ParcelableKeywordBundle createFromParcel(Parcel source) {
            return new ParcelableKeywordBundle(source);
        }

        @Override
        public ParcelableKeywordBundle[] newArray(int size) {
            return new ParcelableKeywordBundle[size];
        }
    };
}
