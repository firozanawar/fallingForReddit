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

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.btmura.android.reddit.database.Subreddits;
import com.btmura.android.reddit.database.Things;
import com.btmura.android.reddit.provider.SubredditProvider;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.util.Array;
import com.btmura.android.reddit.util.Objects;

public class SubredditAdapter extends BaseCursorAdapter {

    public static final String TAG = "SubredditAdapter";

    private static final String[] PROJECTION_SUBREDDITS = {
            Subreddits._ID,
            Subreddits.COLUMN_NAME
    };

    private static final String[] PROJECTION_SEARCH = {
            Things._ID,
            Things.COLUMN_NAME,
            Things.COLUMN_SUBSCRIBERS,
            Things.COLUMN_OVER_18,
    };

    private static final int INDEX_NAME = 1;
    private static final int INDEX_SUBSCRIBERS = 2;
    private static final int INDEX_OVER_18 = 3;

    private String selectedSubreddit;

    public static Loader<Cursor> getLoader(Context context, String accountName,
            String sessionId, String query, boolean sync) {
        Uri uri = getUri(accountName, sessionId, query, sync);
        return getLoader(context, uri, accountName, sessionId, query);
    }

    public static void updateLoader(Context context, Loader<Cursor> loader, String accountName,
            String sessionId, String query, boolean sync) {
        if (loader instanceof CursorLoader) {
            CursorLoader cl = (CursorLoader) loader;
            cl.setUri(getUri(accountName, sessionId, query, sync));
        }
    }

    public static void deleteSessionData(final Context context, final String sessionId,
            String query) {
        if (!TextUtils.isEmpty(query)) {
            final Context appContext = context.getApplicationContext();
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                public void run() {
                    ContentResolver cr = appContext.getContentResolver();
                    cr.delete(ThingProvider.THINGS_URI,
                            Things.SELECT_BY_SESSION_ID, Array.of(sessionId));
                }
            });
        }
    }

    private static Uri getUri(String accountName, String sessionId, String query, boolean sync) {
        if (!TextUtils.isEmpty(query)) {
            return ThingProvider.THINGS_URI.buildUpon()
                    .appendQueryParameter(ThingProvider.PARAM_FETCH, Boolean.toString(sync))
                    .appendQueryParameter(ThingProvider.PARAM_ACCOUNT, accountName)
                    .appendQueryParameter(ThingProvider.PARAM_SESSION_ID, sessionId)
                    .appendQueryParameter(ThingProvider.PARAM_QUERY, query)
                    .build();
        }
        return SubredditProvider.SUBREDDITS_URI;
    }

    private static Loader<Cursor> getLoader(Context context, Uri uri, String accountName,
            String sessionId, String query) {
        if (!TextUtils.isEmpty(query)) {
            return new CursorLoader(context, uri, PROJECTION_SEARCH,
                    Things.SELECT_BY_SESSION_ID, Array.of(sessionId), Things.SORT_BY_NAME);
        }
        return new CursorLoader(context, uri, PROJECTION_SUBREDDITS,
                Subreddits.SELECT_BY_ACCOUNT_NOT_DELETED,
                Array.of(accountName),
                Subreddits.SORT_BY_NAME);
    }

    private final String query;
    private final boolean singleChoice;

    public SubredditAdapter(Context context, String query, boolean singleChoice) {
        super(context, null, 0);
        this.query = query;
        this.singleChoice = singleChoice;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new SubredditView(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(INDEX_NAME);
        int subscribers = query != null ? cursor.getInt(INDEX_SUBSCRIBERS) : -1;
        boolean over18 = query != null && cursor.getInt(INDEX_OVER_18) == 1;
        SubredditView v = (SubredditView) view;
        v.setData(name, over18, subscribers);
        v.setChosen(singleChoice && Objects.equalsIgnoreCase(selectedSubreddit, name));
    }

    public void setSelectedSubreddit(String subreddit) {
        if (!Objects.equals(selectedSubreddit, subreddit)) {
            selectedSubreddit = subreddit;
            notifyDataSetChanged();
        }
    }

    public String setSelectedPosition(int position) {
        String subreddit = getString(position, INDEX_NAME);
        setSelectedSubreddit(subreddit);
        return subreddit;
    }

    public String getName(int position) {
        return getString(position, INDEX_NAME);
    }
}
