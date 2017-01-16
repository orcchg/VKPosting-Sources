package com.orcchg.vikstra.data.source.memory;

import android.content.Context;
import android.os.Environment;

import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.util.Constant;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static File createInternalImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = new StringBuilder("ViKStra_").append(timeStamp).append('_').toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
