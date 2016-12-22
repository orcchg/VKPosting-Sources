package com.orcchg.vikstra.domain.util;

public class Constant {
    public static final long BAD_ID = -1;
    public static final long INIT_ID = 0;

    public static final class RequestCode {
        public static final int EXTERNAL_SCREEN_GALLERY = 9000;
        public static final int EXTERNAL_SCREEN_CAMERA = 9001;

        public static final int GROUP_LIST_SCREEN = 10000;
        public static final int KEYWORD_CREATE_SCREEN = 10010;
        public static final int KEYWORD_LIST_SCREEN = 10011;
        public static final int POST_CREATE_SCREEN = 10020;
        public static final int POST_LIST_SCREEN = 10021;
    }

    public static final class ListTag {
        public static final int GROUP_LIST_SCREEN = 1000;
        public static final int KEYWORD_LIST_SCREEN = 1011;
        public static final int POST_LIST_SCREEN = 1021;
        public static final int POST_SINGLE_GRID_SCREEN = 1022;
    }

    public static final class NotificationID {
        public static final int GROUP_LIST_SCREEN_POSTING = 101;
        public static final int GROUP_LIST_SCREEN_PHOTO_UPLOAD = 102;
    }
}
