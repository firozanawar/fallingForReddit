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

package com.btmura.android.reddit.fragment;

import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.btmura.android.reddit.Debug;
import com.btmura.android.reddit.R;
import com.btmura.android.reddit.activity.SidebarActivity;
import com.btmura.android.reddit.data.Flag;
import com.btmura.android.reddit.entity.Subreddit;
import com.btmura.android.reddit.entity.Thing;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.widget.OnVoteListener;
import com.btmura.android.reddit.widget.ThingAdapter;

public class ThingListFragment extends ListFragment implements
        LoaderCallbacks<Cursor>,
        OnScrollListener,
        OnVoteListener {

    public static final String TAG = "ThingListFragment";
    public static final boolean DEBUG = Debug.DEBUG;

    public static final int FLAG_SINGLE_CHOICE = 0x1;

    private static final String ARG_ACCOUNT_NAME = "an";
    private static final String ARG_SUBREDDIT = "s";
    private static final String ARG_FILTER = "f";
    private static final String ARG_QUERY = "q";
    private static final String ARG_FLAGS = "l";

    private static final String STATE_THING_NAME = "n";
    private static final String STATE_THING_POSITION = "p";

    private static final String LOADER_ARG_MORE_KEY = "m";

    public interface OnThingSelectedListener {
        void onThingSelected(Bundle thingBundle, int position);

        int onMeasureThingBody();
    }

    private String accountName;
    private Subreddit subreddit;
    private int filter;
    private String query;

    private ThingAdapter adapter;
    private OnThingSelectedListener listener;
    private boolean scrollLoading;

    public static ThingListFragment newInstance(String accountName, Subreddit sr, int filter,
            String query, int flags) {
        Bundle args = new Bundle(5);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putParcelable(ARG_SUBREDDIT, sr);
        args.putInt(ARG_FILTER, filter);
        args.putString(ARG_QUERY, query);
        args.putInt(ARG_FLAGS, flags);

        ThingListFragment f = new ThingListFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnThingSelectedListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = savedInstanceState != null ? savedInstanceState : getArguments();
        accountName = b.getString(ARG_ACCOUNT_NAME);
        subreddit = b.getParcelable(ARG_SUBREDDIT);
        filter = b.getInt(ARG_FILTER);
        query = b.getString(ARG_QUERY);

        adapter = new ThingAdapter(getActivity(), Subreddit.getName(subreddit), this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ListView l = (ListView) v.findViewById(android.R.id.list);
        l.setOnScrollListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter.setThingBodyWidth(getThingBodyWidth());
        setListAdapter(adapter);
        setListShown(false);
        loadIfPossible();
    }

    public void loadIfPossible() {
        if (DEBUG) {
            Log.d(TAG, "loadIfPossible an:" + accountName + " s:" + subreddit
                    + " q:" + query + " f:" + filter);
        }
        if (accountName != null && (subreddit != null || query != null)) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri u = ThingAdapter.getInitialUri(getAccountName(), Subreddit.getName(subreddit), filter);
        return ThingAdapter.createLoader(getActivity(), u);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor things) {
        Uri u = ThingAdapter.getUri(getAccountName(), Subreddit.getName(subreddit), filter);
        ((CursorLoader) loader).setUri(u);

        scrollLoading = false;
        adapter.swapCursor(things);
        setEmptyText(getString(things != null ? R.string.empty_list : R.string.error));
        setListShown(true);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (listener != null) {
            listener.onThingSelected(adapter.getThingBundle(position), position);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (visibleItemCount <= 0 || scrollLoading) {
            return;
        }
        if (firstVisibleItem + visibleItemCount * 2 >= totalItemCount) {
            Loader<List<Thing>> loader = getLoaderManager().getLoader(0);
            if (loader != null) {
                if (!adapter.isEmpty()) {
                    // Thing t = adapter.getItem(adapter.getCount() - 1);
                    // if (t.type == Thing.TYPE_MORE) {
                    // scrollLoading = true;
                    // Bundle b = new Bundle(1);
                    // b.putString(LOADER_ARG_MORE_KEY, t.moreKey);
                    // getLoaderManager().restartLoader(0, b, this);
                    // }
                }
            }
        }
    }

    public void onVote(long id, int vote) {
        if (DEBUG) {
            Log.d(TAG, "onVote id: " + id + " vote: " + vote);
        }
        if (!TextUtils.isEmpty(accountName)) {
            ThingProvider.updateLikesInBackground(getActivity(), id, vote);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_ACCOUNT_NAME, accountName);
        outState.putParcelable(ARG_SUBREDDIT, subreddit);
        outState.putInt(ARG_FILTER, filter);
        outState.putString(ARG_QUERY, query);
        // outState.putString(STATE_THING_NAME, adapter.getSelectedThingName());
        // outState.putInt(STATE_THING_POSITION,
        // adapter.getSelectedThingPosition());
    }

    public void setSelectedThing(Bundle thingBundle, int position) {
        // String name = t != null ? t.name : null;
        // if (!adapter.isSelectedThing(name, position)) {
        // adapter.setSelectedThing(name, position);
        // adapter.notifyDataSetChanged();
        // }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thing_list_menu, menu);
        menu.findItem(R.id.menu_view_subreddit_sidebar).setVisible(
                subreddit != null && !subreddit.isFrontPage());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                handleAdd();
                return true;

            case R.id.menu_view_subreddit_sidebar:
                handleViewSidebar();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleAdd() {
    }

    private void handleViewSidebar() {
        Intent intent = new Intent(getActivity(), SidebarActivity.class);
        intent.putExtra(SidebarActivity.EXTRA_SUBREDDIT, getSubreddit());
        startActivity(intent);
    }

    private int getThingBodyWidth() {
        return listener.onMeasureThingBody();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Subreddit getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(Subreddit subreddit) {
        this.subreddit = subreddit;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public String getQuery() {
        return query;
    }

    private int getFlags() {
        return getArguments().getInt(ARG_FLAGS);
    }

    private boolean isSingleChoice() {
        return Flag.isEnabled(getFlags(), FLAG_SINGLE_CHOICE);
    }
}
