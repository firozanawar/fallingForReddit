/*
 * Copyright (C) 2012 Brian Muramatsu
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

public class HideActions implements BaseColumns {

    public static final String TABLE_NAME = "hideActions";

    /** Account that created or deleted this comment. */
    public static final String COLUMN_ACCOUNT = SharedColumns.COLUMN_ACCOUNT;

    /** Action this row represents like adding or deleting. */
    public static final String COLUMN_ACTION = "action";

    /** Unused long column for expiration of this row. */
    public static final String COLUMN_EXPIRATION = "expiration";

    /** ID of the thing that we are marking as read or unread. */
    public static final String COLUMN_THING_ID = "thingId";

    /** Action meaning the user has hidden this thing. */
    public static final int ACTION_HIDE = 0;

    /** Action meaning the user has unhidden this thing. */
    public static final int ACTION_UNHIDE = 1;

    public static final String JOINED_COLUMN_HIDE_ACTION = "hideAction";

    public static final String SELECT_NOT_HIDDEN = JOINED_COLUMN_HIDE_ACTION + " IS NULL";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + COLUMN_ACTION + " INTEGER NOT NULL, "
                + COLUMN_ACCOUNT + " TEXT NOT NULL, "
                + COLUMN_EXPIRATION + " INTEGER DEFAULT 0,"
                + COLUMN_THING_ID + " TEXT NOT NULL,"

                // Add constraint to make it easy to replace actions.
                + "UNIQUE (" + COLUMN_ACCOUNT + "," + COLUMN_THING_ID + "))");
    }
}
