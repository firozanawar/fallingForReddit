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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.R;

public class GlobalMenuFragment extends Fragment
    implements OnFocusChangeListener,
    OnQueryTextListener {

  // TODO: Split this apart into separate fragments.

  public static final String TAG = "GlobalMenuFragment";

  private static final boolean SHOW_DEBUG = BuildConfig.DEBUG && true;

  private static final int REQUEST_SEARCH = 0;

  /** Handler that will accept search queries and perform them. */
  public interface SearchQueryHandler {
    /** Submit a new search query to the handler. */
    boolean submitQuery(String query);

    /** Get the current search query to populate the search query box. */
    String getQuery();
  }

  private SubredditHolder subredditNameHolder;
  private SearchQueryHandler listener;
  private SearchView searchView;
  private MenuItem searchItem;

  public static GlobalMenuFragment newInstance() {
    return new GlobalMenuFragment();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof SubredditHolder) {
      subredditNameHolder = (SubredditHolder) activity;
    }
    if (activity instanceof SearchQueryHandler) {
      listener = (SearchQueryHandler) activity;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.global_menu, menu);
    searchItem = menu.findItem(R.id.menu_search);
    searchView = (SearchView) searchItem.getActionView();
    searchView.setOnQueryTextFocusChangeListener(this);
    searchView.setOnQueryTextListener(this);
    menu.findItem(R.id.menu_debug).setVisible(SHOW_DEBUG);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // GlobalMenuFragment handles more menu items than it presents for
    // convenience. Some other items have complicated visibility logic, so
    // they aren't inflated in this fragment.
    switch (item.getItemId()) {
      case R.id.menu_search:
        handleSearch();
        return true;

      case R.id.menu_debug:
        handleDebug();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  public void handleSearch() {
    searchItem.expandActionView();

    // Set this to the previous query.
    // This MUST come after expandActionView to work.
    if (listener != null) {
      searchView.setQuery(listener.getQuery(), false);
    }
  }

  private void handleDebug() {
    MenuHelper.startContentBrowserActivity(getActivity());
  }

  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus) {
      searchItem.collapseActionView();
    }
  }

  public boolean onQueryTextChange(String newText) {
    return false;
  }

  public boolean onQueryTextSubmit(String query) {
    if (listener != null && listener.submitQuery(query)) {
      searchItem.collapseActionView();
    } else {
      Intent intent = new Intent(getActivity(), SearchActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
          | Intent.FLAG_ACTIVITY_NO_ANIMATION);
      intent.putExtra(SearchActivity.EXTRA_SUBREDDIT,
          subredditNameHolder.getSubreddit());
      intent.putExtra(SearchActivity.EXTRA_QUERY, query);
      startActivityForResult(intent, REQUEST_SEARCH);
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_SEARCH:
        if (searchItem != null) {
          searchItem.collapseActionView();
        }
        break;

      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
