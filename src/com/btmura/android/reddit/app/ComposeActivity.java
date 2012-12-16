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
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;

import com.btmura.android.reddit.R;
import com.btmura.android.reddit.app.CaptchaFragment.OnCaptchaGuessListener;
import com.btmura.android.reddit.app.ComposeFormFragment.OnComposeFormListener;

/**
 * {@link Activity} that displays a form for composing submissions and messages
 * and subsequently processing them.
 */
public class ComposeActivity extends Activity implements OnComposeFormListener,
        OnCaptchaGuessListener {

    /** Charsequence extra for the activity's title */
    public static final String EXTRA_TITLE = "title";

    /** Integer extra indicating the type of composition. */
    public static final String EXTRA_COMPOSITION = "composition";

    /** Type of composition when submitting a link or text. */
    public static final int COMPOSITION_SUBMISSION = ComposeFormFragment.COMPOSITION_SUBMISSION;

    /** Type of composition when replying to some comment. */
    public static final int COMPOSITION_COMMENT = ComposeFormFragment.COMPOSITION_COMMENT;

    /** Type of composition when crafting a message. */
    public static final int COMPOSITION_MESSAGE = ComposeFormFragment.COMPOSITION_MESSAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getIntent().getCharSequenceExtra(EXTRA_TITLE));
        setContentView(R.layout.compose);
        setupActionBar();
        setupFragments(savedInstanceState);
    }

    private void setupActionBar() {
        // No action bar will be available on large devices.
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFragments(Bundle savedInstanceState) {
        // Fragments will be restored on config changes.
        if (savedInstanceState != null) {
            return;
        }

        int composition = getIntent().getIntExtra(EXTRA_COMPOSITION, -1);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.compose_form_container, ComposeFormFragment.newInstance(composition));
        ft.commit();
    }

    public void onComposeForm(String accountName, String destination, String title, String text) {
        Bundle extras = new Bundle();
        CaptchaFragment.newInstance(extras).show(getFragmentManager(), CaptchaFragment.TAG);
    }

    public void onComposeFormCancelled() {
        finish();
    }

    public void onCaptchaGuess(String id, String guess, Bundle extras) {
        switch (getIntent().getIntExtra(EXTRA_COMPOSITION, -1)) {
            case COMPOSITION_SUBMISSION:
                SubmitLinkFragment f = SubmitLinkFragment.newInstance(id, guess, extras);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(f, SubmitLinkFragment.TAG);
                ft.commit();
                break;

            case COMPOSITION_COMMENT:
                break;

            case COMPOSITION_MESSAGE:
                break;

            default:
                throw new IllegalArgumentException();
        }
    }

    public void onCaptchaCancelled() {
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
}
