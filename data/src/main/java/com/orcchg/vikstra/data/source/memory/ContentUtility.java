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

/**
 * In-memory global storage.
 */
public class ContentUtility {

    /* User session */
    // --------------------------------------------------------------------------------------------
    public static class CurrentSession {
        private static long sLastSelectedPostId = Constant.BAD_ID;

        public static void setLastSelectedPostId(long postId) {
            sLastSelectedPostId = postId;
        }

        public static long getLastSelectedPostId() {
            return sLastSelectedPostId;
        }
    }

    /* In-memory storage */
    // --------------------------------------------------------------------------------------------
    public static class InMemoryStorage {
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
    }

    /* Miscellaneous */
    // --------------------------------------------------------------------------------------------
    public static String getFileProviderAuthority() {
        return "com.orcchg.vikstra.fileprovider";  // TODO: get authority from Gradle config
    }

    public static String getDumpGroupsFileName(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        return new StringBuilder(root).append(externalApplicationFolder()).append('/')
                .append("groups_").append(timeStamp).append(".csv").toString();
    }

    public static String getDumpGroupReportsFileName(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String root = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        return new StringBuilder(root).append(externalApplicationFolder()).append('/')
                .append("reports_").append(timeStamp).append(".csv").toString();
    }

    public static File createInternalImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = new StringBuilder("ViKStra_").append(timeStamp).append('_').toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /* Internal */
    // ------------------------------------------
    private static String externalApplicationFolder() {
        return "/vikstra";
    }
}
