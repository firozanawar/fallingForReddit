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

package com.btmura.android.reddit.widget;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import com.btmura.android.reddit.database.SaveActions;
import com.btmura.android.reddit.database.SharedColumns;
import com.btmura.android.reddit.database.Things;
import com.btmura.android.reddit.provider.ThingProvider;

public class CommentAdapter extends LoaderAdapter {

    private static final String[] PROJECTION = {
            Things._ID,
            Things.COLUMN_AUTHOR,
            Things.COLUMN_BODY,
            Things.COLUMN_CREATED_UTC,
            Things.COLUMN_DOWNS,
            Things.COLUMN_EXPANDED,
            Things.COLUMN_KIND,
            Things.COLUMN_LIKES,
            Things.COLUMN_NESTING,
            Things.COLUMN_NUM_COMMENTS,
            Things.COLUMN_PERMA_LINK,
            Things.COLUMN_SAVED,
            Things.COLUMN_SELF,
            Things.COLUMN_SEQUENCE,
            Things.COLUMN_SESSION_ID,
            Things.COLUMN_TITLE,
            Things.COLUMN_THING_ID,
            Things.COLUMN_UPS,
            Things.COLUMN_URL,

            // Following columns are from joined tables at the end.
            SharedColumns.COLUMN_SAVE,
            SharedColumns.COLUMN_VOTE,
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_AUTHOR = 1;
    public static final int INDEX_BODY = 2;
    public static final int INDEX_CREATED_UTC = 3;
    public static final int INDEX_DOWNS = 4;
    public static final int INDEX_EXPANDED = 5;
    public static final int INDEX_KIND = 6;
    public static final int INDEX_LIKES = 7;
    public static final int INDEX_NESTING = 8;
    public static final int INDEX_NUM_COMMENTS = 9;
    public static final int INDEX_PERMA_LINK = 10;
    public static final int INDEX_SAVED = 11;
    public static final int INDEX_SELF = 12;
    public static final int INDEX_SEQUENCE = 13;
    public static final int INDEX_SESSION_ID = 14;
    public static final int INDEX_TITLE = 15;
    public static final int INDEX_THING_ID = 16;
    public static final int INDEX_UPS = 17;
    public static final int INDEX_URL = 18;

    // Following columns are from joined tables at the end.
    public static final int INDEX_SAVE_ACTION = 19;
    public static final int INDEX_VOTE = 20;

    private final long nowTimeMs = System.currentTimeMillis();

    private long sessionId = -1;
    private final String accountName;
    private final String thingId;
    private final String linkId;
    private final OnVoteListener listener;

    public CommentAdapter(Context context, String accountName, String thingId, String linkId,
            OnVoteListener listener) {
        super(context, null, 0);
        this.accountName = accountName;
        this.thingId = thingId;
        this.linkId = linkId;
        this.listener = listener;
    }

    @Override
    public boolean isLoadable() {
        return true;
    }

    @Override
    protected Uri getLoaderUri() {
        return ThingProvider.commentsUri(sessionId, accountName, thingId, linkId);
    }

    @Override
    protected String[] getProjection() {
        return PROJECTION;
    }

    @Override
    protected String getSelection() {
        return Things.SELECT_VISIBLE;
    }

    @Override
    protected String[] getSelectionArgs() {
        return null;
    }

    @Override
    protected String getSortOrder() {
        return Things.SORT_BY_SEQUENCE_AND_ID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new ThingView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String author = cursor.getString(INDEX_AUTHOR);
        String body = cursor.getString(INDEX_BODY);
        long createdUtc = cursor.getLong(INDEX_CREATED_UTC);
        int downs = cursor.getInt(INDEX_DOWNS);
        boolean expanded = cursor.getInt(INDEX_EXPANDED) == 1;
        int kind = cursor.getInt(INDEX_KIND);
        int nesting = cursor.getInt(INDEX_NESTING);
        int numComments = cursor.getInt(INDEX_NUM_COMMENTS);
        String title = cursor.getString(INDEX_TITLE);
        String thingId = cursor.getString(INDEX_THING_ID);
        int ups = cursor.getInt(INDEX_UPS);

        // CommentActions don't have a score so calculate our own.
        int score = ups - downs;

        // TODO: Remove code duplication with ThingAdapter.
        // Local votes take precedence over those from reddit.
        int likes = cursor.getInt(INDEX_LIKES);
        if (!cursor.isNull(INDEX_VOTE)) {
            // Local votes take precedence over those from reddit.
            likes = cursor.getInt(INDEX_VOTE);

            score += likes;
        }

        ThingView tv = (ThingView) view;
        tv.setData(accountName, author, body, createdUtc, null, downs, expanded, kind, likes, null,
                nesting, nowTimeMs, numComments, false, null, score, null, 0, thingId, null, title,
                ups);
        tv.setOnVoteListener(listener);
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean isSaved(int position) {
        // If no local save actions are pending, then rely on server info.
        if (isNull(position, INDEX_SAVE_ACTION)) {
            return getBoolean(position, INDEX_SAVED);
        }

        // We have a local pending action so use that to indicate if it's read.
        return getInt(position, INDEX_SAVE_ACTION) == SaveActions.ACTION_SAVE;
    }

    public String getThingId() {
        return thingId;
    }
}
