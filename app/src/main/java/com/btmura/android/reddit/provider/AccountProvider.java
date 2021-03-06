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

package com.btmura.android.reddit.provider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.accounts.AccountUtils;
import com.btmura.android.reddit.database.AccountActions;
import com.btmura.android.reddit.database.Accounts;
import com.btmura.android.reddit.database.CommentActions;
import com.btmura.android.reddit.database.Comments;
import com.btmura.android.reddit.database.HideActions;
import com.btmura.android.reddit.database.MessageActions;
import com.btmura.android.reddit.database.Messages;
import com.btmura.android.reddit.database.ReadActions;
import com.btmura.android.reddit.database.SaveActions;
import com.btmura.android.reddit.database.Sessions;
import com.btmura.android.reddit.database.SharedColumns;
import com.btmura.android.reddit.database.SubredditResults;
import com.btmura.android.reddit.database.Subreddits;
import com.btmura.android.reddit.database.Things;
import com.btmura.android.reddit.database.VoteActions;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.SubredditResult;
import com.btmura.android.reddit.util.Array;

import java.io.IOException;

public class AccountProvider extends BaseProvider {

  public static final String TAG = "AccountProvider";

  public static final String AUTHORITY =
      "com.btmura.android.reddit.provider.accounts";

  static final String PATH_ACCOUNTS = "accounts";
  static final String PATH_ACCOUNT_ACTIONS = "actions/accounts";

  static final String BASE_AUTHORITY_URI = "content://" + AUTHORITY + "/";
  public static final Uri ACCOUNTS_URI =
      Uri.parse(BASE_AUTHORITY_URI + PATH_ACCOUNTS);
  public static final Uri ACCOUNT_ACTIONS_URI =
      Uri.parse(BASE_AUTHORITY_URI + PATH_ACCOUNT_ACTIONS);

  private static final UriMatcher MATCHER = new UriMatcher(0);
  private static final int MATCH_ACCOUNTS = 1;
  private static final int MATCH_ACCOUNT_ACTIONS = 2;

  static {
    MATCHER.addURI(AUTHORITY, PATH_ACCOUNTS, MATCH_ACCOUNTS);
    MATCHER.addURI(AUTHORITY, PATH_ACCOUNT_ACTIONS, MATCH_ACCOUNT_ACTIONS);
  }

  private static final String METHOD_INITIALIZE_ACCOUNT = "initializeAccount";
  private static final String METHOD_CLEAR_MAIL_INDICATOR =
      "clearMailIndicator";
  private static final String METHOD_REMOVE_ACCOUNT = "removeAccount";

  private static final String[] ACCOUNT_TABLES = {
      Accounts.TABLE_NAME,
      Comments.TABLE_NAME,
      Messages.TABLE_NAME,
      Sessions.TABLE_NAME,
      Subreddits.TABLE_NAME,
      SubredditResults.TABLE_NAME,
      Things.TABLE_NAME,
  };

  private static final String[] ACTION_TABLES = {
      AccountActions.TABLE_NAME,
      CommentActions.TABLE_NAME,
      HideActions.TABLE_NAME,
      MessageActions.TABLE_NAME,
      ReadActions.TABLE_NAME,
      SaveActions.TABLE_NAME,
      VoteActions.TABLE_NAME,
  };

  public AccountProvider() {
    super(TAG);
  }

  @Override
  protected String getTable(Uri uri) {
    switch (MATCHER.match(uri)) {
      case MATCH_ACCOUNTS:
        return Accounts.TABLE_NAME;

      case MATCH_ACCOUNT_ACTIONS:
        return AccountActions.TABLE_NAME;

      default:
        throw new IllegalArgumentException("uri: " + uri);
    }
  }

  /** Initializes an account's subreddits and returns true on success. */
  public static boolean initializeAccount(Context ctx, String accountName) {
    return Provider.call(ctx, ACCOUNTS_URI, METHOD_INITIALIZE_ACCOUNT,
        accountName, null) != null;
  }

  /** Clears an account's mail indicator and returns true on success. */
  public static boolean clearMailIndicator(Context ctx, String accountName) {
    return Provider.call(ctx, ACCOUNTS_URI, METHOD_CLEAR_MAIL_INDICATOR,
        accountName, null) != null;
  }

  /** Removes the account and returns true on success. */
  public static boolean removeAccount(Context ctx, String accountName) {
    return Provider.call(ctx, ACCOUNTS_URI, METHOD_REMOVE_ACCOUNT,
        accountName, null) != null;
  }

  @Override
  public Bundle call(String method, String accountName, Bundle extras) {
    if (METHOD_INITIALIZE_ACCOUNT.equals(method)) {
      return initializeAccount(accountName);
    } else if (METHOD_CLEAR_MAIL_INDICATOR.equalsIgnoreCase(method)) {
      return clearMailIndicator(accountName);
    } else if (METHOD_REMOVE_ACCOUNT.equalsIgnoreCase(method)) {
      return removeAccount(accountName);
    }
    return null;
  }

  /**
   * Returns a non-null empty bundle on successfully creating an account.
   * Otherwise, it returns null on failure whether from getting the user's
   * subreddits or encountering database issues.
   *
   * This method touches many tables that are not the responsibility of
   * AccountProvider, but somebody with access to the database must do this job
   * to assure everything is done in a single transaction.
   */
  private Bundle initializeAccount(String accountName) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "initializeAccount accountName: " + accountName);
    }
    SubredditResult result;
    try {
      result = RedditApi.getMySubreddits(getContext(), accountName);
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    } catch (AuthenticatorException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    } catch (OperationCanceledException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    }

    String selection = SharedColumns.SELECT_BY_ACCOUNT;
    String[] args = Array.of(accountName);

    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      int deleted = 0;
      for (int i = 0; i < ACCOUNT_TABLES.length; i++) {
        deleted += db.delete(ACCOUNT_TABLES[i], selection, args);
      }

      ContentValues v = new ContentValues(3);
      v.put(Subreddits.COLUMN_ACCOUNT, accountName);
      v.put(Subreddits.COLUMN_STATE, Subreddits.STATE_NORMAL);

      for (String subreddit : result.subreddits) {
        v.put(Subreddits.COLUMN_NAME, subreddit);
        db.insert(Subreddits.TABLE_NAME, null, v);
      }

      if (BuildConfig.DEBUG) {
        Log.d(TAG, "deleted: " + deleted
            + " inserted: " + result.subreddits.size());
      }
      db.setTransactionSuccessful();
      return Bundle.EMPTY;
    } finally {
      db.endTransaction();
    }
  }

  /**
   * Clears the account's mail indicator locally and returns an empty bundle. It
   * does not trigger a sync to Reddit.
   */
  private Bundle clearMailIndicator(String accountName) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      // Update the account row which may or may not exist.
      // SyncAdapter will make one later if necessary.
      ContentValues v = new ContentValues(2);
      v.put(Accounts.COLUMN_HAS_MAIL, false);
      db.update(Accounts.TABLE_NAME, v, Accounts.SELECT_BY_ACCOUNT,
          Array.of(accountName));
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }

    // Notify cursors so the indicator disappears but don't sync since that will
    // just make the indicator appear again.
    getContext().getContentResolver().notifyChange(ACCOUNTS_URI, null, NO_SYNC);
    return Bundle.EMPTY;
  }

  private Bundle removeAccount(String accountName) {
    Context ctx = getContext();
    AccountManager am = AccountManager.get(ctx);
    Account account = AccountUtils.getAccount(getContext(), accountName);
    try {
      if (!am.removeAccount(account, null, null).getResult()) {
        return null;
      }
    } catch (OperationCanceledException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    } catch (AuthenticatorException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
      return null;
    }

    String selection = SharedColumns.SELECT_BY_ACCOUNT;
    String[] args = Array.of(accountName);

    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      int deleted = 0;
      for (int i = 0; i < ACCOUNT_TABLES.length; i++) {
        deleted += db.delete(ACCOUNT_TABLES[i], selection, args);
      }
      for (int i = 0; i < ACTION_TABLES.length; i++) {
        deleted += db.delete(ACTION_TABLES[i], selection, args);
      }
      if (BuildConfig.DEBUG) {
        Log.d(TAG, "removeAccount deleted: " + deleted);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }

    return Bundle.EMPTY;
  }
}
