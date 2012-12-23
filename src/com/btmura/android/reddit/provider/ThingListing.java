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
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.database.Kinds;
import com.btmura.android.reddit.database.Things;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.Urls;
import com.btmura.android.reddit.text.Formatter;
import com.btmura.android.reddit.util.JsonParser;

class ThingListing extends JsonParser implements Listing {

    public static final String TAG = "ThingListing";

    private final Formatter formatter = new Formatter();
    private final Context context;
    private final String accountName;
    private final String sessionId;
    private final long sessionTimestamp;
    private final String subreddit;
    private final String query;
    private final String profileUser;
    private final String messageUser;
    private final int filter;
    private final String more;
    private final String cookie;

    private final ArrayList<ContentValues> values = new ArrayList<ContentValues>(30);
    private long networkTimeMs;
    private long parseTimeMs;
    private String moreThingId;

    ThingListing(Context context, String accountName, String sessionId,
            long sessionTimestamp, String subreddit, String query, String profileUser,
            String messageUser, int filter, String more, String cookie) {
        this.context = context;
        this.accountName = accountName;
        this.sessionId = sessionId;
        this.sessionTimestamp = sessionTimestamp;
        this.subreddit = subreddit;
        this.query = query;
        this.profileUser = profileUser;
        this.messageUser = messageUser;
        this.filter = filter;
        this.more = more;
        this.cookie = cookie;
    }

    public int getType() {
        return 0;
    }

    public ArrayList<ContentValues> getValues() throws IOException {
        long t1 = System.currentTimeMillis();

        CharSequence url;
        if (!TextUtils.isEmpty(messageUser)) {
            url = Urls.message(filter, more);
        } else if (!TextUtils.isEmpty(profileUser)) {
            url = Urls.user(profileUser, filter, more);
        } else if (!TextUtils.isEmpty(query)) {
            url = Urls.search(query, more);
        } else {
            url = Urls.subreddit(subreddit, filter, more);
        }

        HttpURLConnection conn = RedditApi.connect(url, cookie, false);
        InputStream input = new BufferedInputStream(conn.getInputStream());
        long t2 = System.currentTimeMillis();
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(input));
            parseListingObject(reader);
            if (BuildConfig.DEBUG) {
                long t3 = System.currentTimeMillis();
                networkTimeMs = t2 - t1;
                parseTimeMs = t3 - t2;
            }
            return values;
        } finally {
            input.close();
            conn.disconnect();
        }
    }

    public long getNetworkTimeMs() {
        return networkTimeMs;
    }

    public long getParseTimeMs() {
        return parseTimeMs;
    }

    @Override
    public void onEntityStart(int index) {
        // Pass -1 and null since we don't know those until later
        values.add(newContentValues(-1, null, 18));
    }

    @Override
    public void onAuthor(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_AUTHOR, readTrimmedString(reader, ""));
    }

    @Override
    public void onBody(JsonReader reader, int index) throws IOException {
        CharSequence body = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Things.COLUMN_BODY, body.toString());
    }

    @Override
    public void onCreatedUtc(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_CREATED_UTC, reader.nextLong());
    }

    @Override
    public void onDomain(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_DOMAIN, readTrimmedString(reader, ""));
    }

    @Override
    public void onDowns(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_DOWNS, reader.nextInt());
    }

    @Override
    public void onKind(JsonReader reader, int index) throws IOException {
        int kindValue = Kinds.parseKind(reader.nextString());
        values.get(index).put(Things.COLUMN_KIND, kindValue);
    }

    @Override
    public void onLikes(JsonReader reader, int index) throws IOException {
        int likes = 0;
        if (reader.peek() == JsonToken.BOOLEAN) {
            likes = reader.nextBoolean() ? 1 : -1;
        } else {
            reader.skipValue();
        }
        values.get(index).put(Things.COLUMN_LIKES, likes);
    }

    @Override
    public void onLinkId(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_LINK_ID, reader.nextString());
    }

    @Override
    public void onLinkTitle(JsonReader reader, int index) throws IOException {
        CharSequence title = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Things.COLUMN_LINK_TITLE, title.toString());
    }

    @Override
    public void onName(JsonReader reader, int index) throws IOException {
        String name = readTrimmedString(reader, "");
        values.get(index).put(Things.COLUMN_THING_ID, name);
    }

    @Override
    public void onNumComments(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_NUM_COMMENTS, reader.nextInt());
    }

    @Override
    public void onOver18(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_OVER_18, reader.nextBoolean());
    }

    @Override
    public void onPermaLink(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_PERMA_LINK, readTrimmedString(reader, ""));
    }

    @Override
    public void onSaved(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_SAVED, reader.nextBoolean());
    }

    @Override
    public void onScore(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_SCORE, reader.nextInt());
    }

    @Override
    public void onIsSelf(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_SELF, reader.nextBoolean());
    }

    @Override
    public void onSubreddit(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_SUBREDDIT, readTrimmedString(reader, ""));
    }

    @Override
    public void onTitle(JsonReader reader, int index) throws IOException {
        CharSequence title = formatter.formatNoSpans(context, readTrimmedString(reader, ""));
        values.get(index).put(Things.COLUMN_TITLE, title.toString());
    }

    @Override
    public void onThumbnail(JsonReader reader, int index) throws IOException {
        String thumbnail = readTrimmedString(reader, null);
        if (!TextUtils.isEmpty(thumbnail) && thumbnail.startsWith("http")) {
            values.get(index).put(Things.COLUMN_THUMBNAIL_URL, thumbnail);
        }
    }

    @Override
    public void onUrl(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_URL, readTrimmedString(reader, ""));
    }

    @Override
    public void onUps(JsonReader reader, int index) throws IOException {
        values.get(index).put(Things.COLUMN_UPS, reader.nextInt());
    }

    @Override
    public void onAfter(JsonReader reader) throws IOException {
        moreThingId = readTrimmedString(reader, null);
    }

    @Override
    public void onParseEnd() {
        if (!TextUtils.isEmpty(moreThingId)) {
            values.add(newContentValues(Kinds.KIND_MORE, moreThingId, 0));
        }
    }

    private ContentValues newContentValues(int kind, String thingId, int extraCapacity) {
        ContentValues v = new ContentValues(5 + extraCapacity);
        v.put(Things.COLUMN_ACCOUNT, accountName);
        v.put(Things.COLUMN_KIND, kind);
        v.put(Things.COLUMN_SESSION_ID, sessionId);
        v.put(Things.COLUMN_SESSION_TIMESTAMP, sessionTimestamp);
        v.put(Things.COLUMN_THING_ID, thingId);
        return v;
    }
}
