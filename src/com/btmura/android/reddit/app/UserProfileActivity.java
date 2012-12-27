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

package com.btmura.android.reddit.app;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountPreferences;
import com.btmura.android.reddit.content.AccountLoader.AccountResult;
import com.btmura.android.reddit.util.Objects;
import com.btmura.android.reddit.widget.FilterAdapter;

/**
 * {@link Activity} for viewing a user's profile.
 */
public class UserProfileActivity extends AbstractBrowserActivity implements OnNavigationListener {

    /** Required string extra that is the user's name. */
    public static final String EXTRA_USER = "user";

    /** Optional int specifying the filter to start using. */
    public static final String EXTRA_FILTER = "filter";

    private static final String STATE_NAVIGATION_INDEX = "navigationIndex";

    private FilterAdapter adapter;
    private String accountName;
    private SharedPreferences prefs;

    @Override
    protected void setContentView() {
        setContentView(R.layout.user_profile);
    }

    @Override
    protected boolean skipSetup() {
        return false;
    }

    @Override
    protected void setupViews() {
    }

    @Override
    protected void setupActionBar(Bundle savedInstanceState) {
        adapter = new FilterAdapter(this);
        adapter.setTitle(getUserName());

        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(adapter, this);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt(STATE_NAVIGATION_INDEX));
        }
    }

    private String getUserName() {
        String user = getIntent().getStringExtra(EXTRA_USER);
        // TODO: Remove this fallback once things become more stable.
        if (user == null) {
            user = "rbbtest1";
        }
        return user;
    }

    @Override
    public void onLoadFinished(Loader<AccountResult> loader, AccountResult result) {
        prefs = result.prefs;
        accountName = result.getLastAccount();
        adapter.addProfileFilters(this);
        if (getIntent().hasExtra(EXTRA_FILTER)) {
            bar.setSelectedNavigationItem(getIntent().getIntExtra(EXTRA_FILTER, 0));
        } else {
            bar.setSelectedNavigationItem(result.getLastProfileFilter());
        }
    }

    public void onLoaderReset(Loader<AccountResult> loader) {
        accountName = null;
    }

    @Override
    protected String getAccountName() {
        return accountName;
    }

    @Override
    protected int getFilter() {
        return adapter.getFilter(bar.getSelectedNavigationIndex());
    }

    @Override
    protected boolean hasSubredditList() {
        return false;
    }

    @Override
    protected void refreshActionBar(String subreddit, Bundle thingBundle) {
    }

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        int filter = getFilter();
        if (!getIntent().hasExtra(EXTRA_FILTER)) {
            AccountPreferences.setLastProfileFilter(prefs, filter);
        } else {
            getIntent().removeExtra(EXTRA_FILTER);
        }

        ThingListFragment frag = getThingListFragment();
        if (frag == null || !Objects.equals(frag.getAccountName(), accountName)
                || frag.getFilter() != filter) {
            setProfileThingListNavigation(getUserName());
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAVIGATION_INDEX, bar.getSelectedNavigationIndex());
    }
}
