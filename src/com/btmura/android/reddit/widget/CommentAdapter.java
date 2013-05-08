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

import com.btmura.android.reddit.accounts.AccountUtils;
import com.btmura.android.reddit.database.Comments;
import com.btmura.android.reddit.database.Kinds;
import com.btmura.android.reddit.database.SaveActions;
import com.btmura.android.reddit.database.SharedColumns;
import com.btmura.android.reddit.provider.Provider;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.text.Formatter;

public class CommentAdapter extends BaseLoaderAdapter {

    private static final String[] PROJECTION = {
            Comments._ID,
            Comments.COLUMN_AUTHOR,
            Comments.COLUMN_BODY,
            Comments.COLUMN_CREATED_UTC,
            Comments.COLUMN_DOMAIN,
            Comments.COLUMN_DOWNS,
            Comments.COLUMN_EXPANDED,
            Comments.COLUMN_KIND,
            Comments.COLUMN_LIKES,
            Comments.COLUMN_NESTING,
            Comments.COLUMN_NUM_COMMENTS,
            Comments.COLUMN_OVER_18,
            Comments.COLUMN_PERMA_LINK,
            Comments.COLUMN_SAVED,
            Comments.COLUMN_SCORE,
            Comments.COLUMN_SELF,
            Comments.COLUMN_SEQUENCE,
            Comments.COLUMN_SESSION_ID,
            Comments.COLUMN_SUBREDDIT,
            Comments.COLUMN_TITLE,
            Comments.TABLE_NAME + "." + Comments.COLUMN_THING_ID,
            Comments.COLUMN_THUMBNAIL_URL,
            Comments.COLUMN_UPS,
            Comments.COLUMN_URL,

            // Following columns are from joined tables at the end.
            SharedColumns.COLUMN_LOCAL_SAVED,
            SharedColumns.COLUMN_LOCAL_VOTE,
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_AUTHOR = 1;
    public static final int INDEX_BODY = 2;
    public static final int INDEX_CREATED_UTC = 3;
    public static final int INDEX_DOMAIN = 4;
    public static final int INDEX_DOWNS = 5;
    public static final int INDEX_EXPANDED = 6;
    public static final int INDEX_KIND = 7;
    public static final int INDEX_LIKES = 8;
    public static final int INDEX_NESTING = 9;
    public static final int INDEX_NUM_COMMENTS = 10;
    public static final int INDEX_OVER_18 = 11;
    public static final int INDEX_PERMA_LINK = 12;
    public static final int INDEX_SAVED = 13;
    public static final int INDEX_SCORE = 14;
    public static final int INDEX_SELF = 15;
    public static final int INDEX_SEQUENCE = 16;
    public static final int INDEX_SESSION_ID = 17;
    public static final int INDEX_SUBREDDIT = 18;
    public static final int INDEX_TITLE = 19;
    public static final int INDEX_THING_ID = 20;
    public static final int INDEX_THUMBNAIL_URL = 21;
    public static final int INDEX_UPS = 22;
    public static final int INDEX_URL = 23;

    // Following columns are from joined tables at the end.
    public static final int INDEX_SAVE_ACTION = 24;
    public static final int INDEX_VOTE = 25;

    private final Formatter formatter = new Formatter();
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
        return Comments.SELECT_VISIBLE;
    }

    @Override
    protected String[] getSelectionArgs() {
        return null;
    }

    @Override
    protected String getSortOrder() {
        return Comments.SORT_BY_SEQUENCE_AND_ID;
    }

    @Override
    public void deleteSessionData(Context context) {
        Provider.deleteSessionAsync(context, ThingProvider.COMMENTS_URI, sessionId);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new ThingView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final String author = cursor.getString(INDEX_AUTHOR);
        final String body = cursor.getString(INDEX_BODY);
        final long createdUtc = cursor.getLong(INDEX_CREATED_UTC);
        final String destination = null; // Only messages have destinations.
        final String domain = null; // Only posts have domains.
        final int downs = cursor.getInt(INDEX_DOWNS);
        final boolean expanded = cursor.getInt(INDEX_EXPANDED) == 1;
        final int kind = cursor.getInt(INDEX_KIND);
        final String linkTitle = null; // Only post replies have link titles.
        final int nesting = cursor.getInt(INDEX_NESTING);
        final int numComments = cursor.getInt(INDEX_NUM_COMMENTS);
        final boolean over18 = cursor.getInt(INDEX_OVER_18) != 0;
        final String parentSubreddit = null; // Comments don't have parent subreddits.
        final String subreddit = null; // Comments don't have subreddits.
        final String title = cursor.getString(INDEX_TITLE);
        final int thingBodyWidth = 0; // Use the full width all the time.
        final String thingId = cursor.getString(INDEX_THING_ID);
        final int ups = cursor.getInt(INDEX_UPS);

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

        final boolean drawVotingArrows = AccountUtils.isAccount(accountName);
        final boolean showThumbnail = false;
        final boolean showStatusPoints = !AccountUtils.isAccount(accountName)
                || cursor.getPosition() != 0;

        ThingView tv = (ThingView) view;
        tv.setType(ThingView.TYPE_COMMENT_LIST);
        tv.setBody(body, false, formatter);
        tv.setData(accountName,
                author,
                createdUtc,
                destination,
                domain,
                downs,
                expanded,
                kind,
                likes,
                linkTitle,
                nesting,
                nowTimeMs,
                numComments,
                over18,
                parentSubreddit,
                score,
                subreddit,
                thingBodyWidth,
                thingId,
                title,
                ups,
                drawVotingArrows,
                showThumbnail,
                showStatusPoints);
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

    public String getThingId() {
        return thingId;
    }

    public String getTitle() {
        return getString(0, INDEX_TITLE);
    }

    public String getLinkUrl() {
        return getString(0, INDEX_URL);
    }

    public boolean isReplyable() {
        return AccountUtils.isAccount(accountName) && getCursor() != null;
    }

    public boolean isSavable() {
        return AccountUtils.isAccount(accountName) && getInt(0, INDEX_KIND) == Kinds.KIND_LINK;
    }

    public boolean isSaved() {
        // If no local save actions are pending, then rely on server info.
        if (isNull(0, INDEX_SAVE_ACTION)) {
            return getBoolean(0, INDEX_SAVED);
        }

        // We have a local pending action so use that to indicate if it's read.
        return getInt(0, INDEX_SAVE_ACTION) == SaveActions.ACTION_SAVE;
    }

    public boolean isSelf() {
        return getBoolean(0, INDEX_SELF);
    }

    public void save(Context context) {
        Provider.saveAsync(context, accountName, thingId,
                getString(0, INDEX_AUTHOR),
                getLong(0, INDEX_CREATED_UTC),
                getString(0, INDEX_DOMAIN),
                getInt(0, INDEX_DOWNS),
                getInt(0, INDEX_LIKES),
                getInt(0, INDEX_NUM_COMMENTS),
                getBoolean(0, INDEX_OVER_18),
                getString(0, INDEX_PERMA_LINK),
                getInt(0, INDEX_SCORE),
                getBoolean(0, INDEX_SELF),
                getString(0, INDEX_SUBREDDIT),
                getString(0, INDEX_TITLE),
                getString(0, INDEX_THUMBNAIL_URL),
                getInt(0, INDEX_UPS),
                getString(0, INDEX_URL));
    }

    public void unsave(Context context) {
        Provider.unsaveAsync(context, accountName, thingId);
    }

    public void vote(Context context, int action, int position) {
        if (position == 0) {
            // Store additional information when the user votes on the header
            // comment which represents the overall thing so that it appears in
            // the liked and disliked listing when the vote is still pending.
            Provider.voteAsync(context, accountName, action,
                    getString(position, INDEX_AUTHOR),
                    getLong(position, INDEX_CREATED_UTC),
                    getString(position, INDEX_DOMAIN),
                    getInt(position, INDEX_DOWNS),
                    getInt(position, INDEX_LIKES),
                    getInt(position, INDEX_NUM_COMMENTS),
                    getBoolean(position, INDEX_OVER_18),
                    getString(position, INDEX_PERMA_LINK),
                    getInt(position, INDEX_SCORE),
                    getBoolean(position, INDEX_SELF),
                    getString(position, INDEX_SUBREDDIT),
                    getString(position, INDEX_THING_ID),
                    getString(position, INDEX_TITLE),
                    getString(position, INDEX_THUMBNAIL_URL),
                    getInt(position, INDEX_UPS),
                    getString(position, INDEX_URL));
        } else {
            // Voting on just the comments won't appear in the liked/disliked
            // listing, so there is no need to send additional info about what
            // we voted upon except the id.
            Provider.voteAsync(context, accountName, action,
                    getString(position, INDEX_THING_ID));
        }
    }
}
