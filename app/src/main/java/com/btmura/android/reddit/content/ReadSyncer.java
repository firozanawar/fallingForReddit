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

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;

import com.btmura.android.reddit.database.ReadActions;
import com.btmura.android.reddit.database.SharedColumns;
import com.btmura.android.reddit.net.RedditApi;
import com.btmura.android.reddit.net.Result;
import com.btmura.android.reddit.provider.ThingProvider;
import com.btmura.android.reddit.util.Array;

import java.io.IOException;

class ReadSyncer implements Syncer {

  private static final String[] PROJECTION = {
      ReadActions._ID,
      ReadActions.COLUMN_ACTION,
      ReadActions.COLUMN_SYNC_FAILURES,
      ReadActions.COLUMN_THING_ID,
  };

  private static final int ID = 0;
  private static final int ACTION = 1;
  private static final int SYNC_FAILURES = 2;
  private static final int THING_ID = 3;

  @Override
  public String getTag() {
    return "r";
  }

  @Override
  public Cursor query(ContentProviderClient provider, String accountName)
      throws RemoteException {
    return provider.query(ThingProvider.READ_ACTIONS_URI,
        PROJECTION,
        SharedColumns.SELECT_BY_ACCOUNT,
        Array.of(accountName),
        null);
  }

  @Override
  public int getSyncFailures(Cursor c) {
    return c.getInt(SYNC_FAILURES);
  }

  @Override
  public Result sync(Context ctx, String accountName, Cursor c)
      throws IOException, AuthenticatorException, OperationCanceledException {
    String thingId = c.getString(THING_ID);
    boolean read = c.getInt(ACTION) == ReadActions.ACTION_READ;
    return RedditApi.readMessage(ctx, accountName, thingId, read);
  }

  @Override
  public void addDeleteAction(Cursor c, Ops ops) {
    long id = c.getLong(ID);
    ops.addDelete(
        ContentProviderOperation.newDelete(ThingProvider.READ_ACTIONS_URI)
            .withSelection(ThingProvider.ID_SELECTION, Array.of(id))
            .build());
  }

  @Override
  public void addUpdateAction(
      Cursor c,
      Ops ops,
      int syncFailures,
      String syncStatus) {
    long id = c.getLong(ID);
    ops.addUpdate(
        ContentProviderOperation.newUpdate(ThingProvider.READ_ACTIONS_URI)
            .withSelection(ThingProvider.ID_SELECTION, Array.of(id))
            .withValue(ReadActions.COLUMN_SYNC_FAILURES, syncFailures)
            .withValue(ReadActions.COLUMN_SYNC_STATUS, syncStatus)
            .build());
  }

  @Override
  public int getEstimatedOpCount(int count) {
    return count;
  }
}
