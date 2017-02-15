package com.orcchg.vikstra.domain.util;

public class Constant {
    public static final int BAD_POSITION = -1;
    public static final long BAD_ID = -1;
    public static final long INIT_ID = 0;
    public static final int NO_ERROR = 0;
    public static final String NO_KEYWORD = "no_keyword";

    public static final int KEYWORDS_LIMIT = 7;
    public static final int MEDIA_ATTACH_LIMIT = 7;
    public static final int GROUPS_COUNT_PER_GET_REQUEST = 1000;
    public static final int GROUPS_LIMIT_FOR_POSTING = 100;

    public static final class RequestCode {
        public static final int EXTERNAL_SCREEN_GALLERY = 9000;
        public static final int EXTERNAL_SCREEN_CAMERA = 9001;

        public static final int GROUP_LIST_SCREEN = 10000;
        public static final int KEYWORD_CREATE_SCREEN = 10010;
        public static final int KEYWORD_LIST_SCREEN = 10011;
        public static final int POST_CREATE_SCREEN = 10020;
        public static final int POST_LIST_SCREEN = 10021;
        public static final int POST_VIEW_SCREEN = 10022;
        public static final int REPORT_SCREEN = 10030;
        public static final int DIALOG_SCREEN = 10050;
    }

    public static final class Broadcast {
        public static final String WALL_POSTING_PROGRESS = "wall_posting_progress";
        public static final String WALL_POSTING_RESULT_DATA = "wall_posting_result_data";
        public static final String WALL_POSTING_STATUS = "wall_posting_status";
        public static final String WALL_POSTING_SUSPEND = "wall_posting_suspend";
    }

    public static final class ListTag {
        public static final int GROUP_LIST_SCREEN = 1000;
        public static final int KEYWORD_CREATE_SCREEN = 1010;
        public static final int KEYWORD_LIST_SCREEN = 1011;
        public static final int POST_CREATE_SCREEN = 1020;
        public static final int POST_LIST_SCREEN = 1021;
        public static final int POST_SINGLE_GRID_SCREEN = 1022;
        public static final int REPORT_SCREEN = 1030;
        public static final int REPORT_HISTORY_SCREEN = 1031;
    }

    public static final class NotificationID {
        public static final int POSTING = 101;
        public static final int PHOTO_UPLOAD = 102;
    }

    public static final class PresenterId {
        public static final int GROUP_LIST_ACTIVITY_PRESENTER = 10000;
        public static final int GROUP_LIST_FRAGMENT_PRESENTER = 10001;
        public static final int KEYWORD_CREATE_PRESENTER = 10010;
        public static final int KEYWORD_LIST_PRESENTER = 10011;
        public static final int POST_CREATE_PRESENTER = 10020;
        public static final int POST_LIST_PRESENTER = 10021;
        public static final int POST_SINGLE_GRID_PRESENTER = 10022;
        public static final int REPORT_PRESENTER = 10030;
        public static final int REPORT_HISTORY_PRESENTER = 10031;
        public static final int MAIN_PRESENTER = 10040;
    }
}
