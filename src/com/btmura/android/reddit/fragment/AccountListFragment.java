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

import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.btmura.android.reddit.Provider.Accounts;
import com.btmura.android.reddit.R;
import com.btmura.android.reddit.preference.AccountPreference;

public class AccountListFragment extends PreferenceFragment implements LoaderCallbacks<Cursor> {

    public static final String TAG = "AccountListFragment";

    private static final String[] PROJECTION = {
            Accounts._ID, Accounts.COLUMN_LOGIN,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Accounts.CONTENT_URI, PROJECTION, null, null,
                Accounts.SORT);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        for (data.moveToPosition(-1); data.moveToNext();) {
            screen.addPreference(new AccountPreference(getActivity(), data.getLong(0), data
                    .getString(1)));
        }
        setPreferenceScreen(screen);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            screen.removeAll();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.account_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_account:
                handleAddAccount();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleAddAccount() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(AddAccountFragment.newInstance(), AddAccountFragment.TAG);
        ft.commit();
    }
}