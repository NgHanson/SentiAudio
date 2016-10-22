package com.microsoft.projectoxford.emotionsample.initialization;

import android.provider.BaseColumns;

/**
 * Created by christophE on 2016-10-22.
 */

public final class SongListContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SongListContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "songentries";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_CATEGORY = "category";
    }
}
