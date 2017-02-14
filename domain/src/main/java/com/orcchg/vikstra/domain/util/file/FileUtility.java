package com.orcchg.vikstra.domain.util.file;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FileUtility {

    @NonNull
    public static File createFileByPath(String path) {
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Timber.e(e, "Failed to create a file by path: %s", path);
            // this error is suppressed here but any further attempt to use this file will lead to IOException
        }
        return file;
    }

    public static String currentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
    }

    public static String getDumpGroupsFileName(Context context, boolean external) {
        return getDumpFileName(context, "groups_", external, true);
    }

    public static String getDumpGroupReportsFileName(Context context, boolean external) {
        return getDumpFileName(context, "reports_", external, true);
    }

    public static String makeDumpFileName(Context context, String name, boolean external) {
        return makeDumpFileName(context, name, external, false);
    }

    public static String makeDumpFileName(Context context, String name, boolean external, boolean withTs) {
        return getDumpFileName(context, name, external, withTs);
    }

    public static String refineExternalPath(String rawPath) {
        int index = rawPath.indexOf(externalApplicationFolder());
        return "/sdcard" + rawPath.substring(index);
    }

    public static Uri uriFromFile(String path) {
        return Uri.fromFile(new File(path));
    }

    /* Internal */
    // ------------------------------------------
    private static String createExternalApplicationFolder(String root) {
        String path = root + externalApplicationFolder();
        File directory = new File(path);
        directory.mkdirs();  // creates directories hierarchy if not exists
        return path;
    }

    private static String externalApplicationFolder() {
        return "/vkposting";
    }

    private static String getDumpFileName(Context context, String prefix, boolean external, boolean withTs) {
        File storage = context.getExternalFilesDir(null);
        String root = external ? Environment.getExternalStorageDirectory().getPath() : (storage != null ? storage.getAbsolutePath() : "");
        String directory = createExternalApplicationFolder(root);
        StringBuilder fileName = new StringBuilder(directory).append('/').append(prefix);
        if (withTs) fileName.append('_').append(currentTimestamp());
        fileName.append(".csv");
        return fileName.toString();
    }
}
