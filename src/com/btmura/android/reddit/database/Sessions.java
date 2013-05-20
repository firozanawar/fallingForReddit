/*
 * Copyright (C) 2013 Brian Muramatsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btmura.android.reddit.database;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * Table containing session data of thing listings whether subreddits, profiles, or messages.
 */
public class Sessions implements BaseColumns {

    public static final String TABLE_NAME = "Sessions";

    /** String identifier of the session like subreddit name or redditor's name. */
    public static final String COLUMN_TAG = "tag";

    /** Integer timestamp of when the session was created. */
    public static final String COLUMN_TIMESTAMP = "timestamp";

    /** Integer type of the listing this session refers to. */
    public static final String COLUMN_TYPE = "type";

    /** Session type when viewing a subreddit. */
    public static final int TYPE_SUBREDDIT = 0;

    /** Session type when viewing a thing's comments. */
    public static final int TYPE_COMMENTS = 1;

    /** Session type when viewing a user's profile. */
    public static final int TYPE_USER = 2;

    /** Session type when searching for things. */
    public static final int TYPE_THING_SEARCH = 3;

    /** Session type when searching for subreddits. */
    public static final int TYPE_SUBREDDIT_SEARCH = 4;

    /** Session type when viewing a user's messages. */
    public static final int TYPE_MESSAGES = 5;

    /** Session type when viewing a message thread. */
    public static final int TYPE_MESSAGE_THREAD = 6;

    public static final String SELECT_BY_ID = _ID + "=?";

    public static final String SELECT_BY_TAG_AND_TYPE =
            COLUMN_TAG + "=? AND " + COLUMN_TYPE + "=?";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TAG + " TEXT NOT NULL,"
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL,"
                + COLUMN_TYPE + ")");
    }

    /** Creates the temporary table used in version 2. Kept for testing upgrades. */
    static void createTempTableV2(SQLiteDatabase db) {
        db.execSQL("CREATE TEMP TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TIMESTAMP + " INTEGER NOT NULL)");
    }
}
