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
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountPreferences;
import com.btmura.android.reddit.content.AccountLoader.AccountResult;
import com.btmura.android.reddit.util.Objects;
import com.btmura.android.reddit.widget.AccountFilterAdapter;
import com.btmura.android.reddit.widget.FilterAdapter;

public class MessageActivity extends AbstractBrowserActivity implements OnNavigationListener {

    /** Required string extra that is the user's name. */
    public static final String EXTRA_USER = "user";

    /** Optional int specifying the filter to start using. */
    public static final String EXTRA_FILTER = "filter";

    private static final String STATE_NAVIGATION_INDEX = "navigationIndex";

    private AccountFilterAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void setContentView() {
        setContentView(R.layout.message);
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
        adapter = new AccountFilterAdapter(this);
        adapter.addMessageFilters(this);

        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(adapter, this);
        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt(STATE_NAVIGATION_INDEX));
        }
    }

    @Override
    public void onLoadFinished(Loader<AccountResult> loader, AccountResult result) {
        prefs = result.prefs;
        adapter.setAccountNames(result.accountNames);

        String accountName = result.getLastAccount();
        if (getIntent().hasExtra(EXTRA_USER)) {
            accountName = getIntent().getStringExtra(EXTRA_USER);
        }
        adapter.setAccountName(accountName);

        int filter = result.getLastMessageFilter();
        if (getIntent().hasExtra(EXTRA_FILTER)) {
            filter = getIntent().getIntExtra(EXTRA_FILTER, FilterAdapter.MESSAGE_INBOX);
        }
        adapter.setFilter(filter);

        int index = adapter.findAccountName(accountName);

        // If the selected navigation index is the same, then the action bar
        // won't fire onNavigationItemSelected. Resetting the adapter and then
        // calling setSelectedNavigationItem again seems to unjam it.
        if (bar.getSelectedNavigationIndex() == index) {
            bar.setListNavigationCallbacks(adapter, this);
        }

        bar.setSelectedNavigationItem(index);
    }

    @Override
    public void onLoaderReset(Loader<AccountResult> loader) {
    }

    @Override
    public String getAccountName() {
        return adapter.getAccountName();
    }

    @Override
    protected int getFilter() {
        return adapter.getFilter();
    }

    @Override
    protected boolean hasSubredditList() {
        return false;
    }

    @Override
    protected void refreshActionBar(String subreddit, Bundle thingBundle) {
    }

    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        adapter.updateState(itemPosition);

        String accountName = adapter.getAccountName();
        if (getIntent().hasExtra(EXTRA_USER)) {
            getIntent().removeExtra(EXTRA_USER);
        }

        int filter = adapter.getFilter();
        if (getIntent().hasExtra(EXTRA_FILTER)) {
            getIntent().removeExtra(EXTRA_FILTER);
        } else {
            AccountPreferences.setLastMessageFilter(prefs, filter);
        }

        ThingListFragment frag = getThingListFragment();
        if (frag == null || !Objects.equals(frag.getAccountName(), accountName)
                || frag.getFilter() != filter) {
            setMessageThingListNavigation(accountName);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean showThingless = isSinglePane || !hasThing();
        menu.setGroupVisible(R.id.thingless, showThingless);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAVIGATION_INDEX, bar.getSelectedNavigationIndex());
    }
}
