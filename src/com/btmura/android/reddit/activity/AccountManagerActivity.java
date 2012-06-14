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

package com.btmura.android.reddit.activity;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.fragment.AccountListFragment;

public class AccountManagerActivity extends Activity implements OnClickListener, AccountManagerCallback<Bundle> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_manager);
        findViewById(R.id.add).setOnClickListener(this);

        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.single_container, AccountListFragment.newInstance(),
                AccountListFragment.TAG);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View v) {
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        manager.addAccount(getString(R.string.account_type), null, null, null, this, this, null);
    }

    public void run(AccountManagerFuture<Bundle> bundle) {
    }
}