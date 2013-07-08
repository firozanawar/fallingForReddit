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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.content.MessageThreadLoader;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.util.ListViewUtils;
import com.btmura.android.reddit.util.Objects;
import com.btmura.android.reddit.widget.MessageThreadAdapter;

public class MessageThreadListController
        implements Controller<MessageThreadAdapter>, ActionModeController {

    static final String EXTRA_ACCOUNT_NAME = "accountName";
    static final String EXTRA_THING_ID = "thingId";
    static final String EXTRA_SESSION_ID = "sessionId";

    private final Context context;
    private final String accountName;
    private final String thingId;
    private final MessageThreadAdapter adapter;
    private long sessionId;
    private ActionMode actionMode;

    MessageThreadListController(Context context, Bundle args) {
        this.context = context;
        this.accountName = getAccountNameExtra(args);
        this.thingId = getThingIdExtra(args);
        this.adapter = new MessageThreadAdapter(context);
    }

    @Override
    public void restoreInstanceState(Bundle savedInstanceState) {
        this.sessionId = savedInstanceState.getLong(EXTRA_SESSION_ID);
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putLong(EXTRA_SESSION_ID, sessionId);
    }

    @Override
    public boolean isLoadable() {
        return accountName != null && !TextUtils.isEmpty(thingId);
    }

    @Override
    public Loader<Cursor> createLoader() {
        return new MessageThreadLoader(context, accountName, thingId, sessionId);
    }

    @Override
    public void swapCursor(Cursor cursor) {
        if (adapter.getCursor() != cursor) {
            adapter.swapCursor(cursor);
            if (cursor != null && cursor.getExtras() != null) {
                Bundle extras = cursor.getExtras();
                sessionId = extras.getLong(ThingProvider.EXTRA_SESSION_ID);
            }
        }
    }

    // Getters

    @Override
    public MessageThreadAdapter getAdapter() {
        return adapter;
    }

    public String getAccountName() {
        return accountName;
    }

    // ActionModeController implementation

    @Override
    public void invalidateActionMode() {
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu, ListView listView) {
        if (adapter.getCursor() == null) {
            listView.clearChoices();
            return false;
        }

        actionMode = mode;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.message_thread_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu, ListView listView) {
        int count = listView.getCheckedItemCount();
        int position = ListViewUtils.getFirstCheckedPosition(listView);

        mode.setTitle(context.getResources().getQuantityString(R.plurals.messages, count, count));
        prepareReplyActionItem(menu, count, position);
        prepareAuthorActionItem(menu, count, position);
        return true;
    }

    private void prepareReplyActionItem(Menu menu, int checkedCount, int position) {
        boolean show = checkedCount == 1
                && !Objects.equals(accountName, adapter.getAuthor(position))
                && !TextUtils.isEmpty(adapter.getThingId(position));
        menu.findItem(R.id.menu_new_comment).setVisible(show);
    }

    private void prepareAuthorActionItem(Menu menu, int checkedCount, int position) {
        String author = adapter.getAuthor(position);
        boolean show = checkedCount == 1 && MenuHelper.isUserItemVisible(author);
        MenuItem item = menu.findItem(R.id.menu_author);
        item.setVisible(show);
        if (item.isVisible()) {
            item.setTitle(MenuHelper.getUserTitle(context, author));
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item, ListView listView) {
        switch (item.getItemId()) {
            case R.id.menu_new_comment:
                handleNewComment(listView);
                mode.finish();
                return true;

            case R.id.menu_author:
                handleAuthor(listView);
                mode.finish();
                return true;

            default:
                return false;
        }
    }

    private void handleNewComment(ListView listView) {
        int position = ListViewUtils.getFirstCheckedPosition(listView);
        String user = adapter.getAuthor(position);

        Bundle extras = new Bundle(2);

        // Message threads are odd in that the thing id doesn't refer to the
        // topmost message, so the actions may not match up with the id. So get
        // the parent id from the first element.
        extras.putString(ComposeActivity.EXTRA_MESSAGE_PARENT_THING_ID, adapter.getThingId(0));
        extras.putString(ComposeActivity.EXTRA_MESSAGE_THING_ID, adapter.getThingId(position));

        MenuHelper.startComposeActivity(context, ComposeActivity.MESSAGE_REPLY_TYPE_SET,
                null, user, null, null, extras, false);
    }

    private void handleAuthor(ListView listView) {
        int position = ListViewUtils.getFirstCheckedPosition(listView);
        MenuHelper.startProfileActivity(context, adapter.getAuthor(position), -1);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
            boolean checked) {
        mode.invalidate();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
    }

    // Getters for extras.

    private static String getAccountNameExtra(Bundle extras) {
        return extras.getString(EXTRA_ACCOUNT_NAME);
    }

    private static String getThingIdExtra(Bundle extras) {
        return extras.getString(EXTRA_THING_ID);
    }

}