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

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountAuthenticator;
import com.btmura.android.reddit.app.CaptchaDialogFragment.OnCaptchaGuessListener;
import com.btmura.android.reddit.content.AccountLoader;
import com.btmura.android.reddit.content.AccountLoader.AccountResult;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.RedditApi.SubmitResult;
import com.btmura.android.reddit.widget.AccountSpinnerAdapter;

public class SubmitLinkActivity extends Activity implements LoaderCallbacks<AccountResult>,
        OnItemSelectedListener, OnCaptchaGuessListener {

    public static final String TAG = "SubmitLinkActivity";

    private AccountSpinnerAdapter adapter;

    private EditText subreddit;
    private EditText title;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_link);
        setupActionBar();
        setupAccountSpinner();
        setupViews();
    }

    private void setupActionBar() {
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupAccountSpinner() {
        adapter = new AccountSpinnerAdapter(this, false);
        Spinner accountSpinner = (Spinner) findViewById(R.id.account_spinner);
        accountSpinner.setAdapter(adapter);
        accountSpinner.setOnItemSelectedListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    private void setupViews() {
        subreddit = (EditText) findViewById(R.id.subreddit);
        title = (EditText) findViewById(R.id.title);
        text = (EditText) findViewById(R.id.text);
    }

    public Loader<AccountResult> onCreateLoader(int id, Bundle args) {
        return new AccountLoader(this, true);
    }

    public void onLoadFinished(Loader<AccountResult> loader, AccountResult result) {
        adapter.setAccountNames(result.accountNames);
    }

    public void onLoaderReset(Loader<AccountResult> loader) {
        adapter.setAccountNames(null);
    }

    public void onItemSelected(AdapterView<?> av, View v, int position, long id) {
        adapter.updateState(position);
    }

    public void onNothingSelected(android.widget.AdapterView<?> av) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.submit_link_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_submit:
                handleSubmit(null, null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleSubmit(String captchaId, String captchaGuess) {
        if (subreddit.getText().length() <= 0) {
            subreddit.setError(getString(R.string.error_blank_field));
            return;
        }
        if (title.getText().length() <= 0) {
            title.setError(getString(R.string.error_blank_field));
            return;
        }
        if (text.getText().length() <= 0) {
            text.setError(getString(R.string.error_blank_field));
            return;
        }

        new SubmitTask(adapter.getAccountName(),
                subreddit.getText().toString(),
                title.getText().toString(),
                text.getText().toString(),
                captchaId,
                captchaGuess).execute();
    }

    class SubmitTask extends AsyncTask<Void, Void, SubmitResult> {

        private final String accountName;
        private final String subreddit;
        private final String title;
        private final String text;
        private final String captchaId;
        private final String captchaGuess;

        SubmitTask(String accountName, String subreddit, String title, String text,
                String captchaId, String captchaGuess) {
            this.accountName = accountName;
            this.subreddit = subreddit;
            this.title = title;
            this.text = text;
            this.captchaId = captchaId;
            this.captchaGuess = captchaGuess;
        }

        @Override
        protected SubmitResult doInBackground(Void... params) {
            try {
                AccountManager manager = AccountManager.get(SubmitLinkActivity.this);
                Account account = new Account(accountName, getString(R.string.account_type));
                String cookie = manager.blockingGetAuthToken(account,
                        AccountAuthenticator.AUTH_TOKEN_COOKIE,
                        true);
                String modhash = manager.blockingGetAuthToken(account,
                        AccountAuthenticator.AUTH_TOKEN_MODHASH,
                        true);
                return RedditApi.submit(subreddit, title, text, captchaId, captchaGuess, cookie,
                        modhash);
            } catch (OperationCanceledException e) {
                Log.e(TAG, "doInBackground", e);
            } catch (AuthenticatorException e) {
                Log.e(TAG, "doInBackground", e);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(SubmitResult result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result.captcha != null) {
                    CaptchaDialogFragment cdf = CaptchaDialogFragment.newInstance(result.captcha);
                    cdf.show(getFragmentManager(), CaptchaDialogFragment.TAG);
                } else if (result.url != null) {
                    Toast.makeText(getApplicationContext(), result.url, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onCaptchaGuess(String id, String guess) {
        handleSubmit(id, guess);
    }

    public void onCaptchaCancelled() {
    }
}