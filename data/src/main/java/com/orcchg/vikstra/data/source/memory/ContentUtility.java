package com.orcchg.vikstra.data.source.memory;

import android.content.Context;
import android.os.Environment;

import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.util.Constant;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * In-memory global storage.
 */
public final class ContentUtility {

    private ContentUtility() {}

    /* User session */
    // --------------------------------------------------------------------------------------------
    public static final class CurrentSession {
        private static long sLastSelectedPostId = Constant.BAD_ID;

        public static void setLastSelectedPostId(long postId) {
            sLastSelectedPostId = postId;
        }

        public static long getLastSelectedPostId() {
            return sLastSelectedPostId;
        }

        private CurrentSession() {}
    }

    /* In-memory storage */
    // --------------------------------------------------------------------------------------------
    public static final class InMemoryStorage {
        /* Instant camera image */
        // --------------------------------------
        private static String sLastStoredInternalImageUrl;

        public static void setLastStoredInternalImageUrl(String url) {
            sLastStoredInternalImageUrl = url;
        }

        public static String getLastStoredInternalImageUrl() {
            return sLastStoredInternalImageUrl;
        }

        /* Parameters for posting */
        // --------------------------------------
        private static List<Group> sSelectedGroupsForPosting;

        public static void setSelectedGroupsForPosting(List<Group> list) {
            sSelectedGroupsForPosting = list;
        }

        public static List<Group> getSelectedGroupsForPosting() {
            return sSelectedGroupsForPosting;
        }

        /* Posting progress & result */
        // --------------------------------------
        private static int sPostingProgress, sPostingTotal;
        private static MultiUseCase.ProgressCallback sProgressCallback;

        public static <Data> void setPostingProgress(int progress, int total, Ordered<Data> data) {
            sPostingProgress = progress;
            sPostingTotal = total;

            if (sProgressCallback != null) sProgressCallback.onDone(progress, total, data);
        }

        public static void setProgressCallback(MultiUseCase.ProgressCallback callback) {
            sProgressCallback = callback;
        }

        public static int getPostingProgress() {
            return sPostingProgress;
        }

        public static int getPostingTotal() {
            return sPostingTotal;
        }

        private InMemoryStorage() {}
    }

    /* Miscellaneous */
    // --------------------------------------------------------------------------------------------
    public static String getFileProviderAuthority() {
        return "com.orcchg.vikstra.fileprovider";  // TODO: get authority from Gradle config
    }

    public static String getDumpGroupsFileName(Context context) {
        return getDumpFileName(context, "groups_");
    }

    public static String getDumpGroupReportsFileName(Context context) {
        return getDumpFileName(context, "reports_");
    }

    public static File createInternalImageFile(Context context) throws IOException {
        String imageFileName = new StringBuilder("ViKStra_").append(currentTimestamp()).append('_').toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /* Internal */
    // ------------------------------------------
    private static String currentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
    }

    private static String createExternalApplicationFolder(String root) {
        String path = root + externalApplicationFolder();
        File directory = new File(path);
        directory.mkdirs();  // creates directories hierarchy if not exists
        return path;
    }

    private static String externalApplicationFolder() {
        return "/vikstra";
    }

    private static String getDumpFileName(Context context, String prefix) {
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        String directory = createExternalApplicationFolder(root);
        String fileName = new StringBuilder(directory).append('/')
                .append(prefix).append(currentTimestamp()).append(".csv").toString();
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Timber.e(e, "Failed to create a file by path: %s", fileName);
            // this error is suppressed here but any further attempt to use this file will lead to IOException
        }
        return fileName;
    }
}
