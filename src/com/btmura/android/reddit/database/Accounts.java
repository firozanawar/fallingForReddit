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

/**
 * Database table for information returned by the /api/me.
 */
public class Accounts implements BaseColumns {

    public static final String TABLE_NAME = "accounts";

    /** Account name of the account. */
    public static final String COLUMN_ACCOUNT = VoteActions.COLUMN_ACCOUNT;

    /** Integer either 0 or 1 indicating whether the account has mail. */
    public static final String COLUMN_HAS_MAIL = "hasMail";

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY,"
                + COLUMN_ACCOUNT + " TEXT NOT NULL,"
                + COLUMN_HAS_MAIL + " INTEGER DEFAULT 0)");
    }
}
