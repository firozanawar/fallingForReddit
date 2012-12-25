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

/**
 * {@link SessionIds} contains definitions of column names and values for tables
 * that have a sessionId column.
 */
public class SessionIds {

    /** Name of the session id column. Use this in table classes for clarity. */
    public static final String COLUMN_SESSION_ID = "sessionId";

    public static final String SELECT_BY_SESSION_ID = COLUMN_SESSION_ID + "=?";

}
