package com.orcchg.vikstra.domain.util.file;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.Keyword;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;

import au.com.bytecode.opencsv.CSVWriter;
import timber.log.Timber;

public class ReportComposer {

    @Inject
    ReportComposer() {
    }

    public boolean writeGroupsToCsv(@Nullable Collection<Group> groups, String path) {
        if (groups == null) {
            Timber.w("Input collection of Group-s is null, nothing to be done");
            return false;
        }
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(path), ';');
            String[] header = new String[]{"", "Group ID", "Keyword", "Link", "Web site", "Members",
                    "Name", "Screen name"};
            writer.writeNext(header);
            for (Group group : groups) {
                Keyword keyword = group.keyword();
                String[] csv = new String[]{
                        Long.toString(group.id()),
                        keyword != null ? keyword.keyword() : "",
                        group.link(),
                        group.webSite(),
                        Integer.toString(group.membersCount()),
                        group.name(),
                        group.screenName()};
                writer.writeNext(csv);
            }
            writer.close();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Failed to write csv to file on path: %s", path);
        }
        return false;
    }

    public boolean writeGroupReportsToCsv(@Nullable Collection<GroupReport> reports, String path) {
        if (reports == null) {
            Timber.w("Input collection of GroupReport-s is null, nothing to be done");
            return false;
        }
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(path), ';');
            String[] header = new String[]{"", "Group ID", "Keyword", "Link", "Web site", "Members",
                    "Name", "Screen name", "Status", "Post ID", "Error code"};
            writer.writeNext(header);
            for (GroupReport report : reports) {
                Group group = report.group();
                Keyword keyword = group.keyword();
                String[] csv = new String[]{
                        Long.toString(group.id()),
                        keyword != null ? keyword.keyword() : "",
                        group.link(),
                        group.webSite(),
                        Integer.toString(group.membersCount()),
                        group.name(),
                        group.screenName(),
                        report.statusString(),
                        "post id: " + Long.toString(report.wallPostId()),
                        "error code: " + report.errorCode()
                };
                writer.writeNext(csv);
            }
            writer.close();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Failed to write csv to file on path: %s", path);
        }
        return false;
    }
}
