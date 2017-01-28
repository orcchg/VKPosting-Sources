package com.orcchg.vikstra.domain.util.file;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.DomainConfig;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.Keyword;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
            Writer io = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "Cp1251"));
            CSVWriter writer = new CSVWriter(io, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
            String[] header = new String[]{" ", "Keyword", "Group ID", "Link", "Members", "Name", "Screen name"};
            writer.writeNext(header);
            int index = 1;
            for (Group group : groups) {
                if (DomainConfig.INSTANCE.useOnlyGroupsWhereCanPostFreely() && !group.canPost()) {
                    continue;  // skip Group-s where is no access for current user to make wall post
                }
                Keyword keyword = group.keyword();
                String[] csv = new String[]{
                        Integer.toString(index),
                        keyword != null ? keyword.keyword() : " ",
                        Long.toString(group.id()),
                        group.link(),
                        Integer.toString(group.membersCount()),
                        group.name().replaceAll("\"", "*"),
                        group.screenName()};
                writer.writeNext(csv);
                ++index;
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
            Writer io = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "Cp1251"));
            CSVWriter writer = new CSVWriter(io, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
            String[] header = new String[]{" ", "Keyword", "Group ID", "Link", "Members", "Name",
                    "Screen name", "Status", "Post ID", "Error code"};
            writer.writeNext(header);
            int index = 1;
            for (GroupReport report : reports) {
                Group group = report.group();
                Keyword keyword = group.keyword();
                String[] csv = new String[]{
                        Integer.toString(index),
                        keyword != null ? keyword.keyword() : " ",
                        Long.toString(group.id()),
                        group.link(),
                        Integer.toString(group.membersCount()),
                        group.name().replaceAll("\"", "*"),
                        group.screenName(),
                        report.statusString(),
                        "post id: " + Long.toString(report.wallPostId()),
                        "error code: " + report.errorCode()
                };
                writer.writeNext(csv);
                ++index;
            }
            writer.close();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Failed to write csv to file on path: %s", path);
        }
        return false;
    }
}
