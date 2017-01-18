package com.orcchg.vikstra.domain.util.file;

import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.Keyword;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.bytecode.opencsv.CSVWriter;
import timber.log.Timber;

@Singleton
public class ReportComposer {

    @Inject
    ReportComposer() {
    }

    public boolean writeGroupsToCsv(Collection<Group> groups, String path) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(path), ',');
            for (Group group : groups) {
                Keyword keyword = group.keyword();
                String[] csv = new String[]{
                        Long.toString(group.id()),
                        keyword != null ? keyword.keyword() : "",
                        group.link(),
                        Integer.toString(group.membersCount()),
                        group.name(),
                        group.systemName()};
                writer.writeNext(csv);
            }
            writer.close();
            return true;
        } catch (IOException e) {
            Timber.e("Failed to write csv to file on path: %s", path);
        }
        return false;
    }

    public boolean writeGroupReportsToCsv(Collection<GroupReport> reports, String path) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(path), ',');
            for (GroupReport report : reports) {
                Group group = report.group();
                String[] csv = new String[]{
                        Long.toString(group.id()),
                        group.link(),
                        Integer.toString(group.membersCount()),
                        group.name(),
                        group.systemName(),
                        report.statusString(),
                        "post id: " + Long.toString(report.wallPostId()),
                        "error code: " + report.errorCode()
                };
                writer.writeNext(csv);
            }
            writer.close();
            return true;
        } catch (IOException e) {
            Timber.e("Failed to write csv to file on path: %s", path);
        }
        return false;
    }
}
