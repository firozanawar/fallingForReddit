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

package com.btmura.android.reddit.provider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.database.CommentActions;
import com.btmura.android.reddit.database.CommentLogic;
import com.btmura.android.reddit.database.CommentLogic.CommentList;
import com.btmura.android.reddit.database.Comments;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.Urls;
import com.btmura.android.reddit.text.Formatter;
import com.btmura.android.reddit.util.Array;
import com.btmura.android.reddit.util.JsonParser;

class CommentListing extends JsonParser implements CommentList {

    public static final String TAG = "CommentListing";

    private static final String[] PROJECTION = {
            CommentActions._ID,
            CommentActions.COLUMN_ACCOUNT,
            CommentActions.COLUMN_ACTION,
            CommentActions.COLUMN_THING_ID,
            CommentActions.COLUMN_TEXT,
    };

    private static final int INDEX_ACCOUNT_NAME = 1;
    private static final int INDEX_ACTION = 2;
    private static final int INDEX_THING_ID = 3;
    private static final int INDEX_TEXT = 4;

    public final ArrayList<ContentValues> values = new ArrayList<ContentValues>(360);
    public long networkTimeMs;
    public long parseTimeMs;

    private final Formatter formatter = new Formatter();
    private final Context context;
    private final SQLiteOpenHelper dbHelper;
    private final String accountName;
    private final String sessionId;
    private final long sessionTimestamp;
    private final String thingId;

    public static CommentListing get(Context context, SQLiteOpenHelper dbHelper,
            String accountName, String sessionId, long sessionTimestamp, String thingId,
            String cookie) throws IOException {
        long t1 = System.currentTimeMillis();
        URL url = Urls.commentsUrl(thingId);
        HttpURLConnection conn = RedditApi.connect(url, cookie, false);
        InputStream input = new BufferedInputStream(conn.getInputStream());
        long t2 = System.currentTimeMillis();
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(input));
            CommentListing listing = new CommentListing(context, dbHelper, accountName, sessionId,
                    sessionTimestamp, thingId);
            listing.parseListingArray(reader);
            if (BuildConfig.DEBUG) {
                long t3 = System.currentTimeMillis();
                listing.networkTimeMs = t2 - t1;
                listing.parseTimeMs = t3 - t2;
            }
            return listing;
        } finally {
            input.close();
            conn.disconnect();
        }
    }

    private CommentListing(Context context, SQLiteOpenHelper dbHelper, String accountName,
            String sessionId, long sessionTimestamp, String thingId) {
        this.context = context;
        this.dbHelper = dbHelper;
        this.accountName = accountName;
        this.sessionId = sessionId;
        this.sessionTimestamp = sessionTimestamp;
        this.thingId = thingId;
    }

    @Override
    public boolean shouldParseReplies() {
        return true;
    }

    @Override
    public void onEntityStart(int index) {
        ContentValues v = new ContentValues(16);
        v.put(Comments.COLUMN_ACCOUNT, accountName);
        v.put(Comments.COLUMN_SEQUENCE, index);
        v.put(Comments.COLUMN_SESSION_ID, sessionId);
        v.put(Comments.COLUMN_SESSION_TIMESTAMP, sessionTimestamp);
        values.add(v);
    }

    @Override
    public void onAuthor(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_AUTHOR, readTrimmedString(reader, ""));
    }

    @Override
    public void onBody(JsonReader reader, int index) throws IOException {
        CharSequence body = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Comments.COLUMN_BODY, body.toString());
    }

    @Override
    public void onCreatedUtc(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_CREATED_UTC, reader.nextLong());
    }

    @Override
    public void onDowns(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_DOWNS, reader.nextInt());
    }

    @Override
    public void onKind(JsonReader reader, int index) throws IOException {
        ContentValues v = values.get(index);
        v.put(Comments.COLUMN_NESTING, replyNesting);

        String kind = reader.nextString();
        if ("more".equalsIgnoreCase(kind)) {
            v.put(Comments.COLUMN_KIND, Comments.KIND_MORE);
        } else if (index != 0) {
            v.put(Comments.COLUMN_KIND, Comments.KIND_COMMENT);
        } else {
            v.put(Comments.COLUMN_KIND, Comments.KIND_HEADER);
        }
    }

    @Override
    public void onLikes(JsonReader reader, int index) throws IOException {
        int likes = 0;
        if (reader.peek() == JsonToken.BOOLEAN) {
            likes = reader.nextBoolean() ? 1 : -1;
        } else {
            reader.skipValue();
        }
        values.get(index).put(Comments.COLUMN_LIKES, likes);
    }

    @Override
    public void onName(JsonReader reader, int index) throws IOException {
        String id = readTrimmedString(reader, "");
        values.get(index).put(Comments.COLUMN_THING_ID, id);
    }

    @Override
    public void onNumComments(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_NUM_COMMENTS, reader.nextInt());
    }

    @Override
    public void onPermaLink(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_PERMA_LINK, reader.nextString());
    }

    @Override
    public void onSelfText(JsonReader reader, int index) throws IOException {
        CharSequence body = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Comments.COLUMN_BODY, body.toString());
    }

    @Override
    public void onTitle(JsonReader reader, int index) throws IOException {
        CharSequence title = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Comments.COLUMN_TITLE, title.toString());
    }

    @Override
    public void onUps(JsonReader reader, int index) throws IOException {
        values.get(index).put(Comments.COLUMN_UPS, reader.nextInt());
    }

    @Override
    public void onParseEnd() {
        // We don't support loading more comments right now.
        removeMoreComments();

        // Merge local inserts and deletes that haven't been synced yet.
        mergeActions();
    }

    private void removeMoreComments() {
        int size = values.size();
        for (int i = 0; i < size;) {
            ContentValues v = values.get(i);
            Integer type = (Integer) v.get(Comments.COLUMN_KIND);
            if (type.intValue() == Comments.KIND_MORE) {
                values.remove(i);
                size--;
            } else {
                i++;
            }
        }
    }

    private void mergeActions() {
        // Track the fake number of insertions and deletions.
        int delta = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(CommentActions.TABLE_NAME, PROJECTION,
                CommentActions.SELECT_BY_PARENT_THING_ID, Array.of(thingId),
                null, null, CommentActions.SORT_BY_ID);
        try {
            while (c.moveToNext()) {
                String actionAccountName = c.getString(INDEX_ACCOUNT_NAME);
                int action = c.getInt(INDEX_ACTION);
                String id = c.getString(INDEX_THING_ID);
                String text = c.getString(INDEX_TEXT);
                switch (action) {
                    case CommentActions.ACTION_INSERT:
                        if (insertThing(actionAccountName, id, text)) {
                            delta++;
                        }
                        break;

                    case CommentActions.ACTION_DELETE:
                        if (deleteThing(id)) {
                            delta--;
                        }
                        break;

                    default:
                        throw new IllegalStateException();
                }
            }
        } finally {
            c.close();
        }

        // Update the header comment count with our fake inserts and deletes.
        Integer numComments = (Integer) values.get(0).get(Comments.COLUMN_NUM_COMMENTS);
        values.get(0).put(Comments.COLUMN_NUM_COMMENTS, numComments.intValue() + delta);
    }

    private boolean insertThing(String actionAccountName, String replyId, String body) {
        int size = values.size();
        for (int i = 0; i < size; i++) {
            ContentValues v = values.get(i);
            String id = v.getAsString(Comments.COLUMN_THING_ID);

            // This thing could be a placeholder we previously inserted.
            if (TextUtils.isEmpty(id)) {
                continue;
            }

            if (id.equals(replyId)) {
                ContentValues p = new ContentValues(8);
                p.put(Comments.COLUMN_ACCOUNT, actionAccountName);
                p.put(Comments.COLUMN_AUTHOR, actionAccountName);
                p.put(Comments.COLUMN_BODY, body);
                p.put(Comments.COLUMN_KIND, Comments.KIND_COMMENT);
                p.put(Comments.COLUMN_NESTING, CommentLogic.getInsertNesting(this, i));
                p.put(Comments.COLUMN_SEQUENCE, CommentLogic.getInsertSequence(this, i));
                p.put(Comments.COLUMN_SESSION_ID, sessionId);
                p.put(Comments.COLUMN_SESSION_TIMESTAMP, sessionTimestamp);

                values.add(CommentLogic.getInsertPosition(this, i), p);
                size++;

                return true;
            }
        }
        return false;
    }

    private boolean deleteThing(String deleteId) {
        int size = values.size();
        for (int i = 0; i < size; i++) {
            ContentValues v = values.get(i);
            String id = v.getAsString(Comments.COLUMN_THING_ID);
            if (deleteId.equals(id)) {
                if (CommentLogic.hasChildren(this, i)) {
                    v.put(Comments.COLUMN_AUTHOR, Comments.DELETED);
                    v.put(Comments.COLUMN_BODY, Comments.DELETED);
                    return false;
                } else {
                    values.remove(i);
                    size--;
                    return true;
                }
            }
        }
        return false;
    }

    public int getCommentCount() {
        return values.size();
    }

    public long getCommentId(int position) {
        // Cast to avoid auto-boxing in the getAsLong method.
        return ((Long) values.get(position).get(Comments._ID)).longValue();
    }

    public int getCommentNesting(int position) {
        // Cast to avoid auto-boxing in the getAsInteger method.
        return ((Integer) values.get(position).get(Comments.COLUMN_NESTING)).intValue();
    }

    public int getCommentSequence(int position) {
        // Cast to avoid auto-boxing in the getAsInteger method.
        return ((Integer) values.get(position).get(Comments.COLUMN_SEQUENCE)).intValue();
    }
}