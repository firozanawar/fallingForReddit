/*
 * Copyright (C) 2013 Brian Muramatsu
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

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountUtils;
import com.btmura.android.reddit.database.Subreddits;

// TODO(btmura): Split this apart into separate classes for profile and subreddits.
class ThingTableMenuController implements MenuController {

  private final Context ctx;
  private final FragmentManager fragmentManager;
  private final String accountName;
  private final SubredditHolder subredditNameHolder;
  private final ThingHolder thingHolder;
  private final Refreshable refreshable;

  ThingTableMenuController(
      Context ctx,
      FragmentManager fragmentManager,
      String accountName,
      SubredditHolder subredditNameHolder,
      ThingHolder thingHolder,
      Refreshable refreshable) {
    this.ctx = ctx;
    this.fragmentManager = fragmentManager;
    this.accountName = accountName;
    this.subredditNameHolder = subredditNameHolder;
    this.thingHolder = thingHolder;
    this.refreshable = refreshable;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.thing_table_menu, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    String subreddit = getSubreddit();

    boolean hasAccount = AccountUtils.isAccount(accountName);
    boolean hasSubreddit = subreddit != null;
    boolean hasThing = thingHolder != null && thingHolder.isShowingThing();
    boolean hasSidebar = Subreddits.hasSidebar(subreddit);
    boolean isSubreddit = hasSubreddit && !hasThing;

    boolean showNewPost = isSubreddit && hasAccount;
    boolean showSubreddit = isSubreddit && hasSidebar;
    boolean showRefresh = !hasThing;

    menu.findItem(R.id.menu_add_subreddit).setVisible(isSubreddit);
    menu.findItem(R.id.menu_new_post).setVisible(showNewPost);
    menu.findItem(R.id.menu_refresh).setVisible(showRefresh);

    MenuItem subredditItem = menu.findItem(R.id.menu_subreddit);
    subredditItem.setVisible(showSubreddit);
    if (showSubreddit) {
      subredditItem.setTitle(MenuHelper.getSubredditTitle(ctx, subreddit));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_new_post:
        handleNewPost();
        return true;

      case R.id.menu_refresh:
        handleRefresh();
        return true;

      case R.id.menu_subreddit:
        handleSubreddit();
        return true;

      case R.id.menu_add_subreddit:
        handleAddSubreddit();
        return true;

      default:
        return false;
    }
  }

  private void handleNewPost() {
    MenuHelper.startNewPostComposer(ctx, accountName, getSubreddit());
  }

  private void handleRefresh() {
    refreshable.refresh();
  }

  private void handleSubreddit() {
    MenuHelper.startSidebarActivity(ctx, getSubreddit());
  }

  private void handleAddSubreddit() {
    MenuHelper.showAddSubredditDialog(fragmentManager, getSubreddit());
  }

  private String getSubreddit() {
    return subredditNameHolder.getSubreddit();
  }
}
