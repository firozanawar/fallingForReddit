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

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.accounts.AccountAuthenticator;
import com.btmura.android.reddit.entity.LoginResult;
import com.btmura.android.reddit.provider.NetApi;
import com.btmura.android.reddit.provider.SubredditProvider;
import com.btmura.android.reddit.provider.SyncAdapterService;
import com.btmura.android.reddit.text.InputFilters;

public class AddAccountFragment extends Fragment implements
        OnCheckedChangeListener,
        OnClickListener {

    public static final String TAG = "AddAccountFragment";

    private static final InputFilter[] INPUT_FILTERS = new InputFilter[] {
            InputFilters.LOGIN_FILTER,
    };

    public interface OnAccountAddedListener {
        void onAccountAdded(Bundle result);

        void onAccountCancelled();
    }

    private OnAccountAddedListener listener;

    private EditText login;
    private EditText password;
    private Button add;
    private Button cancel;

    public static AddAccountFragment newInstance() {
        return new AddAccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnAccountAddedListener) {
            listener = (OnAccountAddedListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_account, container, false);

        login = (EditText) v.findViewById(R.id.login);
        login.setFilters(INPUT_FILTERS);

        password = (EditText) v.findViewById(R.id.password);

        CheckBox showPassword = (CheckBox) v.findViewById(R.id.show_password);
        showPassword.setOnCheckedChangeListener(this);

        cancel = (Button) v.findViewById(R.id.cancel);
        cancel.setOnClickListener(this);

        add = (Button) v.findViewById(R.id.add);
        add.setOnClickListener(this);

        return v;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        TransformationMethod method = null;
        if (!isChecked) {
            method = PasswordTransformationMethod.getInstance();
        }
        password.setTransformationMethod(method);
    }

    public void onClick(View v) {
        if (v == cancel) {
            handleCancel();
        } else if (v == add) {
            handleAdd();
        }
    }

    private void handleCancel() {
        if (listener != null) {
            listener.onAccountCancelled();
        }
    }

    private void handleAdd() {
        if (login.getText().length() <= 0) {
            login.setError(getString(R.string.error_blank_field));
            return;
        }
        if (password.getText().length() <= 0) {
            password.setError(getString(R.string.error_blank_field));
            return;
        }
        if (login.getError() == null && password.getError() == null && listener != null) {
            new LoginTask(login.toString(), password.toString()).execute();
        }
    }

    class LoginTask extends AsyncTask<Void, Integer, Bundle> {

        private final String login;
        private final String password;
        private final ProgressDialog progress;

        LoginTask(String login, String password) {
            this.login = login;
            this.password = password;
            this.progress = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            progress.setMessage(getString(R.string.authenticator_logging_in));
            progress.show();
        }

        @Override
        protected Bundle doInBackground(Void... params) {
            try {
                LoginResult result = NetApi.login(getActivity(), login, password);
                if (result.error != null) {
                    return errorBundle(R.string.authenticator_reddit_error, result.error);
                }

                publishProgress(R.string.authenticator_adding_account);

                String accountType = AccountAuthenticator.getAccountType(getActivity());
                Account account = new Account(login, accountType);

                AccountManager manager = AccountManager.get(getActivity());
                manager.addAccountExplicitly(account, null, null);
                manager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_COOKIE, result.cookie);
                manager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_MODHASH,
                        result.modhash);

                ContentResolver.setSyncAutomatically(account, SubredditProvider.AUTHORITY, true);

                Bundle extras = new Bundle(2);
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                extras.putBoolean(SyncAdapterService.EXTRA_INITIAL_SYNC, true);
                ContentResolver.requestSync(account, SubredditProvider.AUTHORITY, extras);

                Bundle b = new Bundle(2);
                b.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                b.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                return b;

            } catch (IOException e) {
                Log.e(TAG, "doInBackground", e);
                return errorBundle(R.string.authenticator_error, e.getMessage());
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress.setMessage(getString(values[0]));
        }

        @Override
        protected void onCancelled(Bundle result) {
            progress.dismiss();
        }

        @Override
        protected void onPostExecute(Bundle result) {
            progress.dismiss();

            String error = result.getString(AccountManager.KEY_ERROR_MESSAGE);
            if (error != null) {
                SimpleDialogFragment.showMessage(getFragmentManager(), error);
            } else if (listener != null) {
                listener.onAccountAdded(result);
            }
        }

        private Bundle errorBundle(int resId, String... formatArgs) {
            Bundle b = new Bundle(1);
            b.putString(AccountManager.KEY_ERROR_MESSAGE, getString(resId, (Object[]) formatArgs));
            return b;
        }
    }
}
