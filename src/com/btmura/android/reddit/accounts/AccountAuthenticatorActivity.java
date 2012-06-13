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

package com.btmura.android.reddit.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.fragment.AddAccountFragment;
import com.btmura.android.reddit.fragment.AddAccountFragment.OnAccountAddedListener;

public class AccountAuthenticatorActivity extends android.accounts.AccountAuthenticatorActivity
        implements OnAccountAddedListener {

    public static final String TAG = "AccountAuthenticatorActivity";

    private static final String AUTH_TOKEN_COOKIE = "cookie";
    private static final String AUTH_TOKEN_MODHASH = "modhash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_authenticator);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.single_container, AddAccountFragment.newInstance());
        ft.commit();
    }

    public void onAccountAdded(String login, String cookie, String modhash) {
        Log.d(TAG, "onAccountAdded");

        String accountType = getString(R.string.account_type);
        Account account = new Account(login, accountType);

        AccountManager manager = AccountManager.get(this);
        manager.addAccountExplicitly(account, null, null);
        manager.setAuthToken(account, AUTH_TOKEN_COOKIE, cookie);
        manager.setAuthToken(account, AUTH_TOKEN_MODHASH, modhash);

        Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, login);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        setAccountAuthenticatorResult(result);

        setResult(RESULT_OK);
        finish();
    }
}
