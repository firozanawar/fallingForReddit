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
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.btmura.android.reddit.net.Urls;
import com.btmura.android.reddit.text.Formatter;
import com.btmura.android.reddit.util.BundleSupport;
import com.btmura.android.reddit.util.StringUtil;

/**
 * {@link ThingBundle} is a {@link Bundle} representation of some thing. This class adds type
 * information to the underlying {@link Bundle}s attributes and nothing more.
 */
public class ThingBundle extends BundleSupport implements Parcelable {

    private static final int NUM_KEYS = 20;

    private static final String KEY_AUTHOR = "author";
    private static final String KEY_CREATED_UTC = "createdUtc";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_DOWNS = "downs";
    private static final String KEY_KIND = "kind";
    private static final String KEY_LIKES = "likes";
    private static final String KEY_LINK_ID = "linkId";
    private static final String KEY_LINK_TITLE = "linkTitle";
    private static final String KEY_NUM_COMMENTS = "numComments";
    private static final String KEY_OVER_18 = "over18";
    private static final String KEY_PERMA_LINK = "permaLink";
    private static final String KEY_SAVED = "saved";
    private static final String KEY_SCORE = "score";
    private static final String KEY_SELF = "self";
    private static final String KEY_SUBREDDIT = "subreddit";
    private static final String KEY_THING_ID = "thingId";
    private static final String KEY_THUMBNAIL_URL = "thumbnailUrl";
    private static final String KEY_TITLE = "title";
    private static final String KEY_UPS = "ups";
    private static final String KEY_URL = "url";

    public static final Parcelable.Creator<ThingBundle> CREATOR =
            new Parcelable.Creator<ThingBundle>() {
                public ThingBundle createFromParcel(Parcel in) {
                    return new ThingBundle(in);
                }

                public ThingBundle[] newArray(int size) {
                    return new ThingBundle[size];
                }
            };

    private final Bundle data;

    private Formatter formatter;

    public static ThingBundle newCommentsUrlInstance(String subreddit, String thingId) {
        Bundle data = new Bundle(NUM_KEYS);
        data.putString(KEY_SUBREDDIT, subreddit);
        data.putString(KEY_THING_ID, thingId);
        return new ThingBundle(data);
    }

    public static ThingBundle newCommentsUrlInstance(String subreddit, String thingId,
            String linkId) {
        Bundle data = new Bundle(NUM_KEYS);
        data.putString(KEY_SUBREDDIT, subreddit);
        data.putString(KEY_THING_ID, thingId);
        data.putString(KEY_LINK_ID, linkId);
        return new ThingBundle(data);
    }

    public static ThingBundle newLinkInstance(String author,
            long createdUtc,
            String domain,
            int downs,
            int likes,
            int kind,
            String linkId,
            String linkTitle,
            int numComments,
            boolean over18,
            String permaLink,
            boolean saved,
            int score,
            boolean self,
            String subreddit,
            String thingId,
            String thumbnailUrl,
            String title,
            int ups,
            String url) {
        Bundle data = new Bundle(NUM_KEYS);
        data.putString(KEY_AUTHOR, author);
        data.putLong(KEY_CREATED_UTC, createdUtc);
        data.putString(KEY_DOMAIN, domain);
        data.putInt(KEY_DOWNS, downs);
        data.putInt(KEY_KIND, kind);
        data.putString(KEY_LINK_ID, linkId);
        data.putString(KEY_LINK_TITLE, linkTitle);
        data.putInt(KEY_LIKES, likes);
        data.putInt(KEY_NUM_COMMENTS, numComments);
        data.putBoolean(KEY_OVER_18, over18);
        data.putString(KEY_PERMA_LINK, permaLink);
        data.putBoolean(KEY_SAVED, saved);
        data.putInt(KEY_SCORE, score);
        data.putBoolean(KEY_SELF, self);
        data.putString(KEY_SUBREDDIT, subreddit);
        data.putString(KEY_THING_ID, thingId);
        data.putString(KEY_THUMBNAIL_URL, thumbnailUrl);
        data.putString(KEY_TITLE, title);
        data.putInt(KEY_UPS, ups);
        data.putString(KEY_URL, url);
        return new ThingBundle(data);
    }

    protected ThingBundle(ThingBundle thingBundle) {
        this(thingBundle.data);
    }

    private ThingBundle(Parcel in) {
        this(in.readBundle());
    }

    private ThingBundle(Bundle data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(data);
    }

    // TODO: Move these methods with logic to a different class.

    public CharSequence getCommentsUrl() {
        return Urls.perma(getPermaLink(), null);
    }

    public String getDisplayTitle(Context context) {
        String title = getTitle();
        return !TextUtils.isEmpty(title)
                ? format(context, title)
                : format(context, getLinkTitle());
    }

    private String format(Context context, String text) {
        if (formatter == null) {
            formatter = new Formatter();
        }
        return StringUtil.safeString(formatter.formatAll(context, text));
    }

    public CharSequence getLinkUrl() {
        return getUrl();
    }

    public boolean hasCommentsUrl() {
        return true;
    }

    public boolean hasLinkUrl() {
        return !isSelf();
    }

    // Simple getters that return attributes as they are

    public String getAuthor() {
        return getString(data, KEY_AUTHOR);
    }

    public long getCreatedUtc() {
        return getLong(data, KEY_CREATED_UTC);
    }

    public String getDomain() {
        return getString(data, KEY_DOMAIN);
    }

    public int getDowns() {
        return getInt(data, KEY_DOWNS);
    }

    public int getKind() {
        return getInt(data, KEY_KIND);
    }

    public int getLikes() {
        return getInt(data, KEY_LIKES);
    }

    public String getLinkId() {
        return getString(data, KEY_LINK_ID);
    }

    public String getLinkTitle() {
        return getString(data, KEY_LINK_TITLE);
    }

    public int getNumComments() {
        return getInt(data, KEY_NUM_COMMENTS);
    }

    public String getPermaLink() {
        return getString(data, KEY_PERMA_LINK);
    }

    public int getScore() {
        return getInt(data, KEY_SCORE);
    }

    public String getSubreddit() {
        return getString(data, KEY_SUBREDDIT);
    }

    public String getTitle() {
        return getString(data, KEY_TITLE);
    }

    public String getThingId() {
        return getString(data, KEY_THING_ID);
    }

    public String getThumbnailUrl() {
        return getString(data, KEY_THUMBNAIL_URL);
    }

    public int getUps() {
        return getInt(data, KEY_UPS);
    }

    public String getUrl() {
        return getString(data, KEY_URL);
    }

    public boolean isOver18() {
        return data.getBoolean(KEY_OVER_18);
    }

    public boolean isSaved() {
        return data.getBoolean(KEY_SAVED);
    }

    public boolean isSelf() {
        return data.getBoolean(KEY_SELF);
    }
}
