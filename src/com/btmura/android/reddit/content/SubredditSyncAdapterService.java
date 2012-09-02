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

package com.btmura.android.reddit.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.btmura.android.reddit.BuildConfig;
import com.btmura.android.reddit.accounts.AccountAuthenticator;
import com.btmura.android.reddit.database.Subreddits;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.provider.SubredditProvider;

public class SubredditSyncAdapterService extends Service {

    public static final String TAG = "SubredditSyncAdapterService";

    private static final String[] PROJECTION = {
            Subreddits._ID,
            Subreddits.COLUMN_NAME,
            Subreddits.COLUMN_STATE,
            Subreddits.COLUMN_EXPIRATION,
    };

    private static final int INDEX_ID = 0;
    private static final int INDEX_NAME = 1;
    private static final int INDEX_STATE = 2;
    private static final int INDEX_EXPIRATION = 3;

    @Override
    public IBinder onBind(Intent intent) {
        return new SubredditSyncAdapter(this).getSyncAdapterBinder();
    }

    static class SubredditSyncAdapter extends AbstractThreadedSyncAdapter {

        public SubredditSyncAdapter(Context context) {
            super(context, true);
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority,
                ContentProviderClient provider, SyncResult syncResult) {

            int numInserts = 0;
            int numUpdates = 0;
            int numDeletes = 0;
            int numEntries = 0;

            AccountManager manager = AccountManager.get(getContext());
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            try {
                String cookie = manager.blockingGetAuthToken(account,
                        AccountAuthenticator.AUTH_TOKEN_COOKIE,
                        true);

                ArrayList<String> subreddits = RedditApi.getSubreddits(cookie);

                Cursor c = provider.query(SubredditProvider.CONTENT_URI, PROJECTION,
                        SubredditProvider.SELECTION_ACCOUNT,
                        new String[] {account.name},
                        null);
                while (c.moveToNext()) {
                    long expiration = c.getLong(INDEX_EXPIRATION);
                    boolean expired = expiration != 0
                            && System.currentTimeMillis() > expiration;

                    String name = c.getString(INDEX_NAME);
                    int index = find(subreddits, name);
                    boolean exists = index != -1;
                    if (exists) {
                        subreddits.remove(index);
                    }

                    long id = c.getLong(INDEX_ID);
                    int state = c.getInt(INDEX_STATE);
                    switch (state) {
                        case Subreddits.STATE_INSERTING:
                        case Subreddits.STATE_DELETING:
                            if (expired) {
                                if (exists) {
                                    ops.add(newUpdateToNormalState(id));
                                    numUpdates++;
                                } else {
                                    ops.add(newDeleteById(id));
                                    numDeletes++;
                                }
                                numEntries++;
                            }
                            break;

                        case Subreddits.STATE_NORMAL:
                            if (!exists) {
                                ops.add(newDeleteById(id));
                                numDeletes++;
                                numEntries++;
                            }
                            break;
                    }
                }
                c.close();

                if (!subreddits.isEmpty()) {
                    int count = subreddits.size();
                    for (int i = 0; i < count; i++) {
                        ops.add(newInsert(account.name, subreddits.get(i), Subreddits.STATE_NORMAL));
                        numInserts++;
                        numEntries++;
                    }
                }

                ContentResolver cr = getContext().getContentResolver();
                cr.applyBatch(SubredditProvider.AUTHORITY, ops);

                syncResult.stats.numInserts += numInserts;
                syncResult.stats.numUpdates += numUpdates;
                syncResult.stats.numDeletes += numDeletes;
                syncResult.stats.numEntries += numEntries;

            } catch (OperationCanceledException e) {
                Log.e(TAG, "onPerformSync", e);
                syncResult.stats.numAuthExceptions++;
            } catch (AuthenticatorException e) {
                Log.e(TAG, "onPerformSync", e);
                syncResult.stats.numAuthExceptions++;
            } catch (IOException e) {
                Log.e(TAG, "onPerformSync", e);
                syncResult.stats.numIoExceptions++;
            } catch (RemoteException e) {
                Log.e(TAG, "onPerformSync", e);
                syncResult.databaseError = true;
            } catch (OperationApplicationException e) {
                Log.e(TAG, "onPerformSync", e);
                syncResult.databaseError = true;
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "accountName: " + account.name + " syncResult: " + syncResult.toString());
            }
        }

        private static int find(List<String> subreddits, String name) {
            int count = subreddits.size();
            for (int i = 0; i < count; i++) {
                if (name.equalsIgnoreCase(subreddits.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        private static ContentProviderOperation newUpdateToNormalState(long id) {
            return ContentProviderOperation.newUpdate(SubredditProvider.CONTENT_URI)
                    .withSelection(SubredditProvider.ID_SELECTION,
                            new String[] {Long.toString(id)})
                    .withValue(Subreddits.COLUMN_STATE, Subreddits.STATE_NORMAL)
                    .withValue(Subreddits.COLUMN_EXPIRATION, 0)
                    .build();
        }

        private static ContentProviderOperation newDeleteById(long id) {
            return ContentProviderOperation.newDelete(SubredditProvider.CONTENT_URI)
                    .withSelection(SubredditProvider.ID_SELECTION,
                            new String[] {Long.toString(id)})
                    .build();
        }

        private static ContentProviderOperation newInsert(String accountName, String subredditName,
                int state) {
            return ContentProviderOperation.newInsert(SubredditProvider.CONTENT_URI)
                    .withValue(Subreddits.COLUMN_ACCOUNT, accountName)
                    .withValue(Subreddits.COLUMN_NAME, subredditName)
                    .withValue(Subreddits.COLUMN_STATE, state)
                    .build();
        }
    }

    public static void initializeAccount(Context context, String login, String cookie)
            throws RemoteException, OperationApplicationException, IOException {
        ArrayList<String> subreddits = RedditApi.getSubreddits(cookie);
        int count = subreddits.size();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>(
                count + 2);
        ops.add(ContentProviderOperation.newDelete(SubredditProvider.CONTENT_URI)
                .withSelection(SubredditProvider.SELECTION_ACCOUNT, new String[] {login})
                .build());
        ops.add(ContentProviderOperation.newInsert(SubredditProvider.CONTENT_URI)
                .withValue(Subreddits.COLUMN_ACCOUNT, login)
                .withValue(Subreddits.COLUMN_NAME, Subreddits.NAME_FRONT_PAGE)
                .withValue(Subreddits.COLUMN_STATE, Subreddits.STATE_INSERTING)
                .build());
        for (int i = 0; i < count; i++) {
            ops.add(ContentProviderOperation.newInsert(SubredditProvider.CONTENT_URI)
                    .withValue(Subreddits.COLUMN_ACCOUNT, login)
                    .withValue(Subreddits.COLUMN_NAME, subreddits.get(i))
                    .withValue(Subreddits.COLUMN_STATE, Subreddits.STATE_NORMAL)
                    .build());
        }
        ContentResolver cr = context.getContentResolver();
        cr.applyBatch(SubredditProvider.AUTHORITY, ops);
    }
}