package com.orcchg.vikstra.data.source.memory;

import android.content.Context;
import android.os.Environment;

import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.io.File;
import java.io.IOException;

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

        private InMemoryStorage() {}
    }

    /* Files */
    // --------------------------------------------------------------------------------------------
    public static File createInternalImageFile(Context context) throws IOException {
        String imageFileName = new StringBuilder("ViKStra_").append(FileUtility.currentTimestamp()).append('_').toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName, ".jpg", storageDir);
        file.deleteOnExit();  // delete this file when JVM terminates normally
        return file;
    }

    public static String getFileProviderAuthority() {
        return "com.orcchg.dev.maxa.vikstra.fileprovider";  // TODO: get authority from Gradle config
    }
}
