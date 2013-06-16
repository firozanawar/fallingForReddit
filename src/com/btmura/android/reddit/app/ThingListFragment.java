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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountUtils;
import com.btmura.android.reddit.database.Subreddits;
import com.btmura.android.reddit.util.Objects;
import com.btmura.android.reddit.view.SwipeDismissTouchListener;
import com.btmura.android.reddit.view.SwipeDismissTouchListener.OnSwipeDismissListener;
import com.btmura.android.reddit.widget.OnVoteListener;
import com.btmura.android.reddit.widget.ThingListAdapter;
import com.btmura.android.reddit.widget.ThingView;

public class ThingListFragment extends ThingProviderListFragment implements
        OnScrollListener, OnSwipeDismissListener, OnVoteListener, MultiChoiceModeListener {

    public static final String TAG = "ThingListFragment";

    /** Integer argument specifying the fragment type. */
    private static final String ARG_TYPE = "type";

    /** String argument specifying the account being used. */
    private static final String ARG_ACCOUNT_NAME = "accountName";

    /** String argument specifying the parent subreddit. */
    private static final String ARG_PARENT_SUBREDDIT = "parentSubreddit";

    /** String argument specifying the subreddit to load. */
    private static final String ARG_SUBREDDIT = "subreddit";

    /** String argument specifying the search query to use. */
    private static final String ARG_QUERY = "query";

    /** String argument specifying the profileUser profile to load. */
    private static final String ARG_PROFILE_USER = "profileUser";

    /** Integer argument to filter things, profile, or messages. */
    private static final String ARG_FILTER = "filter";

    /** Boolean argument indicating whether the list should be put into single choice mode. */
    private static final String ARG_SINGLE_CHOICE = "singleChoice";

    private static final int TYPE_SUBREDDIT = 1;
    private static final int TYPE_SEARCH = 2;
    private static final int TYPE_PROFILE = 3;
    private static final int TYPE_MESSAGES = 4;

    /** String argument that is used to paginate things. */
    private static final String LOADER_MORE_ID = "moreId";

    public interface OnThingSelectedListener {
        void onThingSelected(View view, Bundle thingBundle, int pageType);

        int onMeasureThingBody();
    }

    private OnThingSelectedListener listener;
    private OnSubredditEventListener eventListener;
    private ThingBundleHolder thingBundleHolder;
    private ThingListController controller;
    private boolean scrollLoading;

    public static ThingListFragment newSubredditInstance(String accountName, String subreddit,
            int filter, boolean singleChoice) {
        Bundle args = new Bundle(6);
        args.putInt(ARG_TYPE, TYPE_SUBREDDIT);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putString(ARG_PARENT_SUBREDDIT, subreddit);
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putInt(ARG_FILTER, filter);
        args.putBoolean(ARG_SINGLE_CHOICE, singleChoice);
        return newFragment(args);
    }

    public static ThingListFragment newSearchInstance(String accountName, String subreddit,
            String query, boolean singleChoice) {
        Bundle args = new Bundle(6);
        args.putInt(ARG_TYPE, TYPE_SEARCH);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putString(ARG_PARENT_SUBREDDIT, subreddit);
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putString(ARG_QUERY, query);
        args.putBoolean(ARG_SINGLE_CHOICE, singleChoice);
        return newFragment(args);
    }

    public static ThingListFragment newProfileInstance(String accountName, String profileUser,
            int filter, boolean singleChoice) {
        Bundle args = new Bundle(5);
        args.putInt(ARG_TYPE, TYPE_PROFILE);
        args.putString(ARG_ACCOUNT_NAME, accountName);
        args.putString(ARG_PROFILE_USER, profileUser);
        args.putInt(ARG_FILTER, filter);
        args.putBoolean(ARG_SINGLE_CHOICE, singleChoice);
        return newFragment(args);
    }

    public static ThingListFragment newMessageInstance(String accountName, String messageUser,
            int filter, boolean singleChoice) {
        Bundle args = new Bundle(5);
        args.putInt(ARG_TYPE, TYPE_MESSAGES);
        args.putString(MessageThingListController.EXTRA_ACCOUNT_NAME, accountName);
        args.putString(MessageThingListController.EXTRA_MESSAGE_USER, messageUser);
        args.putInt(MessageThingListController.EXTRA_FILTER, filter);
        args.putBoolean(MessageThingListController.EXTRA_SINGLE_CHOICE, singleChoice);
        return newFragment(args);
    }

    private static ThingListFragment newFragment(Bundle args) {
        ThingListFragment frag = new ThingListFragment();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnThingSelectedListener) {
            listener = (OnThingSelectedListener) activity;
        }
        if (activity instanceof OnSubredditEventListener) {
            eventListener = (OnSubredditEventListener) activity;
        }
        if (activity instanceof ThingBundleHolder) {
            thingBundleHolder = (ThingBundleHolder) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThingListAdapter adapter = new ThingListAdapter(getActivity(), this,
                getSingleChoiceArgument());

        controller = createController(adapter);
        if (savedInstanceState != null) {
            controller.restoreInstanceState(savedInstanceState);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        SwipeDismissTouchListener touchListener = new SwipeDismissTouchListener(listView, this);
        listView.setOnTouchListener(touchListener);

        final OnScrollListener scrollListener = touchListener.makeScrollListener();
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisible, int visibleCount,
                    int totalCount) {
                scrollListener.onScroll(view, firstVisible, visibleCount, totalCount);
                ThingListFragment.this.onScroll(view, firstVisible, visibleCount, totalCount);
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrollListener.onScrollStateChanged(view, scrollState);
                ThingListFragment.this.onScrollStateChanged(view, scrollState);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        controller.setThingBodyWidth(getThingBodyWidth());
        setListAdapter(controller.getAdapter());
        setListShown(false);
        if (controller.getEmptyText() == 0) {
            loadIfPossible();
        } else {
            showEmpty();
        }
    }

    public void loadIfPossible() {
        if (controller.isLoadable()) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    public void setEmpty(boolean error) {
        controller.setEmptyText(error ? R.string.error : R.string.empty_list);
        showEmpty();
    }

    private void showEmpty() {
        setEmptyText(getString(controller.getEmptyText()));
        setListShown(true);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        args = Objects.nullToEmpty(args);
        controller.setMoreId(args.getString(LOADER_MORE_ID));
        return controller.createLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (controller.swapCursor(cursor)) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "onLoadFinished");
            }

            // TODO: Remove dependency on ThingProviderListFragment.
            super.onLoadFinished(loader, cursor);

            scrollLoading = false;

            setEmptyText(getString(cursor != null ? R.string.empty_list : R.string.error));
            setListShown(true);
            getActivity().invalidateOptionsMenu();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        controller.swapCursor(null);
    }

    @Override
    protected void onSubredditLoaded(String subreddit) {
        controller.setParentSubreddit(subreddit);
        controller.setSubreddit(subreddit);
        if (eventListener != null) {
            eventListener.onSubredditDiscovery(subreddit);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        if (visibleItemCount <= 0 || scrollLoading) {
            return;
        }
        if (firstVisibleItem + visibleItemCount * 2 >= totalItemCount
                && getLoaderManager().getLoader(0) != null
                && controller.hasNextMoreId()) {
            scrollLoading = true;
            Bundle args = new Bundle(1);
            args.putString(LOADER_MORE_ID, controller.getNextMoreId());
            getLoaderManager().restartLoader(0, args, this);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selectThing(v, position, ThingPagerAdapter.TYPE_LINK);
        if (controller.isSingleChoice() && v instanceof ThingView) {
            ((ThingView) v).setChosen(true);
        }
    }

    private void selectThing(View v, int position, int pageType) {
        controller.setSelectedPosition(position);
        if (listener != null) {
            listener.onThingSelected(v, controller.getThingBundle(position), pageType);
        }
        controller.select(position);
    }

    @Override
    public boolean isSwipeDismissable(int position) {
        return controller.isSwipeDismissable(position);
    }

    @Override
    public void onSwipeDismiss(ListView listView, View view, int position) {
        controller.hide(position, true);
    }

    public void onVote(View v, int action) {
        if (controller.hasAccountName()) {
            int position = getListView().getPositionForView(v);
            controller.vote(position, action);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thing_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        String subreddit = getSubreddit();
        boolean isQuery = isQuery();
        boolean hasAccount = AccountUtils.isAccount(getAccountName());
        boolean hasSubreddit = subreddit != null;
        boolean hasThing = thingBundleHolder != null && thingBundleHolder.getThingBundle() != null;
        boolean hasSidebar = Subreddits.hasSidebar(subreddit);

        boolean showNewPost = !isQuery && hasAccount && hasSubreddit && !hasThing;
        boolean showAddSubreddit = !isQuery && hasSubreddit && !hasThing;
        boolean showSubreddit = !isQuery && hasSubreddit && !hasThing && hasSidebar;

        menu.findItem(R.id.menu_new_post).setVisible(showNewPost);
        menu.findItem(R.id.menu_add_subreddit).setVisible(showAddSubreddit);

        MenuItem subredditItem = menu.findItem(R.id.menu_subreddit);
        subredditItem.setVisible(showSubreddit);
        if (showSubreddit) {
            subredditItem.setTitle(MenuHelper.getSubredditTitle(getActivity(), subreddit));
        }
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (!controller.hasCursor()) {
            getListView().clearChoices();
            return false;
        }
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.thing_action_menu, menu);
        return true;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        int count = getListView().getCheckedItemCount();
        mode.setTitle(getResources().getQuantityString(R.plurals.things, count, count));
        controller.prepareActionMenu(menu, getListView(), getFirstCheckedPosition());
        return true;
    }

    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mode.invalidate();
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_saved:
                controller.save(getFirstCheckedPosition(), false);
                mode.finish();
                return true;

            case R.id.menu_unsaved:
                controller.save(getFirstCheckedPosition(), true);
                mode.finish();
                return true;

            case R.id.menu_hide:
                controller.hide(getFirstCheckedPosition(), true);
                mode.finish();
                return true;

            case R.id.menu_unhide:
                controller.hide(getFirstCheckedPosition(), false);
                mode.finish();
                return true;

            case R.id.menu_comments:
                handleComments();
                mode.finish();
                return true;

            case R.id.menu_copy_url:
                controller.copyUrl(getFirstCheckedPosition());
                mode.finish();
                return true;

            case R.id.menu_author:
                controller.author(getFirstCheckedPosition());
                mode.finish();
                return true;

            case R.id.menu_subreddit:
                controller.subreddit(getFirstCheckedPosition());
                mode.finish();
                return true;

            default:
                return false;
        }
    }

    private void handleComments() {
        selectThing(null, getFirstCheckedPosition(), ThingPagerAdapter.TYPE_COMMENTS);
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        controller.saveInstanceState(outState);
    }

    private int getThingBodyWidth() {
        return listener != null ? listener.onMeasureThingBody() : 0;
    }

    public void setAccountName(String accountName) {
        controller.setAccountName(accountName);
    }

    public String getAccountName() {
        return controller.getAccountName();
    }

    public void setSelectedThing(String thingId, String linkId) {
        controller.setSelectedThing(thingId, linkId);
    }

    public String getSubreddit() {
        return controller.getSubreddit();
    }

    public void setSubreddit(String subreddit) {
        if (!Objects.equalsIgnoreCase(subreddit, controller.getSubreddit())) {
            controller.setSubreddit(subreddit);
        }
    }

    public String getQuery() {
        return controller.getQuery();
    }

    public boolean isQuery() {
        return !TextUtils.isEmpty(getQuery());
    }

    public int getFilter() {
        return controller.getFilter();
    }

    private int getFirstCheckedPosition() {
        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        int size = controller.getAdapter().getCount();
        for (int i = 0; i < size; i++) {
            if (checked.get(i)) {
                return i;
            }
        }
        return -1;
    }

    // Factory method for creating a ThingListController for the fragment.
    private ThingListController createController(ThingListAdapter adapter) {
        switch (getTypeArgument()) {
            case TYPE_SUBREDDIT:
                return new SubredditThingListController(getActivity(), getArguments(), adapter);

            case TYPE_SEARCH:
                return new SearchThingListController(getActivity(), getArguments(), adapter);

            case TYPE_PROFILE:
                return new ProfileThingListController(getActivity(),
                        getProfileUserArgument(),
                        getArguments(),
                        adapter);

            case TYPE_MESSAGES:
                return new MessageThingListController(getActivity(), getArguments());

            default:
                throw new IllegalArgumentException();
        }
    }

    // Getters for fragment arguments.

    private int getTypeArgument() {
        return getArguments().getInt(ARG_TYPE);
    }

    private String getProfileUserArgument() {
        return getArguments().getString(ARG_PROFILE_USER);
    }

    private boolean getSingleChoiceArgument() {
        return getArguments().getBoolean(ARG_SINGLE_CHOICE);
    }
}
