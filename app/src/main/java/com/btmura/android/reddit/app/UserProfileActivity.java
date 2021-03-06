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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.content.AccountLoader;
import com.btmura.android.reddit.content.AccountLoader.AccountResult;
import com.btmura.android.reddit.content.ThemePrefs;
import com.btmura.android.reddit.content.UserInfoLoader;
import com.btmura.android.reddit.net.AccountInfoResult;
import com.btmura.android.reddit.net.UriHelper;
import com.btmura.android.reddit.util.Array;
import com.btmura.android.reddit.widget.AccountFilterAdapter;

/**
 * {@link Activity} for viewing a user's profile.
 */
public class UserProfileActivity extends AbstractBrowserActivity implements
    LoaderCallbacks<AccountResult>,
    OnNavigationListener {

  private static final String STATE_FILTER = "filter";

  private final LoaderCallbacks<AccountInfoResult> karmaLoaderCallbacks =
      new LoaderCallbacks<AccountInfoResult>() {
        public Loader<AccountInfoResult> onCreateLoader(int id, Bundle args) {
          return new UserInfoLoader(getApplicationContext(), accountName,
              currentUser);
        }

        public void onLoadFinished(
            Loader<AccountInfoResult> loader,
            AccountInfoResult result) {
          int[] linkKarma = result != null ? new int[]{result.linkKarma} : null;
          int[] commentKarma = result != null ? new int[]{result.commentKarma} : null;
          adapter.setAccountInfo(Array.of(currentUser), linkKarma, commentKarma,
              null);
        }

        public void onLoaderReset(Loader<AccountInfoResult> loader) {
        }
      };

  private String currentUser;
  private int currentFilter = -1;
  private AccountFilterAdapter adapter;
  private String accountName;

  public UserProfileActivity() {
    super(UserProfileThingActivity.class);
  }

  @Override
  protected void setContentView() {
    setTheme(ThemePrefs.getTheme(this));
    setContentView(R.layout.profile);
  }

  @Override
  protected boolean skipSetup(Bundle savedInstanceState) {
    // Get the user from the intent data or extra.
    Uri data = getIntent().getData();
    if (data != null) {
      currentUser = UriHelper.getUser(data);
      currentFilter = UriHelper.getUserFilter(data);
    }

    // Quit if there is no profile to view.
    if (TextUtils.isEmpty(currentUser)) {
      finish();
      return true;
    }

    // Continue on since we have some user.
    return false;
  }

  @Override
  protected void doSetup(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      currentFilter = savedInstanceState.getInt(STATE_FILTER);
    }

    adapter = new AccountFilterAdapter(this);

    // Don't show additional account-only profile filters.
    adapter.addProfileFilters(this, false);

    // Set the only account option to the user we are viewing not us.
    adapter.setAccountInfo(Array.of(currentUser), null, null, null);

    if (currentFilter == -1) {
      currentFilter = Filter.PROFILE_OVERVIEW;
    }
    adapter.setFilter(currentFilter);

    bar.setListNavigationCallbacks(adapter, this);
    bar.setDisplayHomeAsUpEnabled(true);
    bar.setDisplayShowTitleEnabled(false);
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

    getSupportLoaderManager().initLoader(0, null, this);
  }

  @Override
  public Loader<AccountResult> onCreateLoader(int id, Bundle args) {
    return new AccountLoader(this, true, false);
  }

  @Override
  public void onLoadFinished(
      Loader<AccountResult> loader,
      AccountResult result) {
    accountName = result.getLastAccount(this);
    getSupportLoaderManager().initLoader(1, null, karmaLoaderCallbacks);

    // Reset the adapter to trigger a selection callback since there is
    // only 1 account.
    bar.setListNavigationCallbacks(adapter, this);
    bar.setSelectedNavigationItem(0);
  }

  @Override
  public void onLoaderReset(Loader<AccountResult> loader) {
  }

  @Override
  public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    if (accountName != null) {
      adapter.updateState(itemPosition);
      currentFilter = adapter.getFilter();
      setUserProfileFragments(accountName, currentUser, currentFilter);
    }
    return true;
  }

  @Override
  public String getAccountName() {
    return accountName;
  }

  @Override
  protected boolean hasLeftFragment() {
    return false;
  }

  @Override
  protected void refreshActionBar(ControlFragment controlFrag) {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.profile_menu, menu);
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
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_new_message:
        handleNewMessage();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void handleNewMessage() {
    MenuHelper.startNewMessageComposer(this, accountName, currentUser);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(STATE_FILTER, currentFilter);
  }
}
