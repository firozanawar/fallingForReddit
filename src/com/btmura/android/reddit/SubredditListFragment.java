package com.btmura.android.reddit;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ListView;
import android.widget.Toast;

public class SubredditListFragment extends ListFragment implements LoaderCallbacks<Cursor>, MultiChoiceModeListener {
	
	private static final String ARGS_SINGLE_CHOICE = "singleChoice";

	interface OnSubredditSelectedListener {
		static final int FLAG_LOAD_FINISHED = 0;
		static final int FLAG_ITEM_CLICKED = 1;
		void onSubredditSelected(Subreddit sr, int event);
	}

	private SubredditAdapter adapter;
	private OnSubredditSelectedListener listener;

	public static SubredditListFragment newInstance(boolean singleChoice) {
		SubredditListFragment frag = new SubredditListFragment();
		Bundle args = new Bundle(1);
		args.putBoolean(ARGS_SINGLE_CHOICE, singleChoice);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (OnSubredditSelectedListener) activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new SubredditAdapter(getActivity(), getArguments().getBoolean(ARGS_SINGLE_CHOICE));	
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		ListView list = (ListView) view.findViewById(android.R.id.list);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		list.setMultiChoiceModeListener(this);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(adapter);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return SubredditAdapter.createLoader(getActivity());
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		setListShown(true);
		if (data.getCount() > 0) {
			getListView().post(new Runnable() {
				public void run() {
					Subreddit sr = Subreddit.newInstance(adapter.getName(getActivity(), 0));
					listener.onSubredditSelected(sr, OnSubredditSelectedListener.FLAG_LOAD_FINISHED);
				}
			});
		}
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Subreddit sr = Subreddit.newInstance(adapter.getName(getActivity(), position));
		adapter.setSelectedSubreddit(sr);
		listener.onSubredditSelected(sr, OnSubredditSelectedListener.FLAG_ITEM_CLICKED);
	}
	
	class CheckedInfo {
		int checkedCount;
		int numSplittable;
	}
	
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.subreddit, menu);
		menu.findItem(R.id.menu_add).setVisible(false);
		if (mode.getTag() == null) {
			mode.setTag(new CheckedInfo());
		}
		return true;
	}
	
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		CheckedInfo info = (CheckedInfo) mode.getTag();
		menu.findItem(R.id.menu_combine).setVisible(info.checkedCount > 1);
		menu.findItem(R.id.menu_split).setVisible(info.checkedCount == 1 && info.numSplittable > 0);	
		return true;
	}
		
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_combine:
			handleCombined(mode);
			return true;
			
		case R.id.menu_split:
			handleSplit(mode);
			return true;
			
		case R.id.menu_delete:
			handleDelete(mode);
			return true;
		}
		return false;
	}
	
	private void handleCombined(ActionMode mode) {
		ArrayList<String> names = getCheckedNames();
		int size = names.size();
		if (size <= 1) {
			Toast.makeText(getActivity().getApplicationContext(), 
					getString(R.string.num_subreddits_added, 0), 
					Toast.LENGTH_SHORT).show();
			mode.finish();
			return;
		}
	
		long[] ids = getListView().getCheckedItemIds();
		
		Provider.combineSubredditsInBackground(getActivity(), names, ids);
		showToast(size - 1, false);
		mode.finish();
	}
	
	private void handleSplit(ActionMode mode) {
		ArrayList<String> names = getCheckedNames();
		long[] ids = getListView().getCheckedItemIds();
		Provider.splitSubredditInBackground(getActivity(), names.get(0), ids[0]);
		showToast(1, false);
		mode.finish();
	}
	
	private void handleDelete(ActionMode mode) {
		long[] ids = getListView().getCheckedItemIds();
		Provider.deleteSubredditInBackground(getActivity(), ids);
		showToast(ids.length, false);
		mode.finish();
	}
	
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		CheckedInfo info = (CheckedInfo) mode.getTag();
		info.checkedCount = getListView().getCheckedItemCount();		
		if (adapter.getName(getActivity(), position).indexOf('+') != -1) {
			info.numSplittable = info.numSplittable + (checked ? 1 : -1);
		}
		mode.invalidate();
	}
	
	public void onDestroyActionMode(ActionMode mode) {
	}
	
	public void setSelectedSubreddit(Subreddit subreddit) {
		adapter.setSelectedSubreddit(subreddit);
		adapter.notifyDataSetChanged();
	}
	
	private ArrayList<String> getCheckedNames() {
		int checkedCount = getListView().getCheckedItemCount();
		SparseBooleanArray checked = getListView().getCheckedItemPositions();
		ArrayList<String> names = new ArrayList<String>(checkedCount);
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			if (checked.get(i)) {
				String name = adapter.getName(getActivity(), i);
				if (!name.isEmpty()) {
					names.add(name);
				}
			}
		}
		return names;
	}
	
	private void showToast(int count, boolean added) {
		Toast.makeText(getActivity().getApplicationContext(), 
				getString(added ? R.string.num_subreddits_added : R.string.num_subreddits_deleted, count), 
				Toast.LENGTH_SHORT).show();
	}
}
