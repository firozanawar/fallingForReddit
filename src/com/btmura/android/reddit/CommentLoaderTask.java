package com.btmura.android.reddit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.util.JsonReader;
import android.util.Log;


public class CommentLoaderTask extends AsyncTask<Entity, Void, ArrayList<Entity>> {
	
	private static final String TAG = "CommentLoaderTask";

	private final Context context;
	private final TaskListener<ArrayList<Entity>> listener;

	public CommentLoaderTask(Context context, TaskListener<ArrayList<Entity>> listener) {
		this.context = context;
		this.listener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		listener.onPreExecute();
	}

	@Override
	protected void onPostExecute(ArrayList<Entity> comments) {
		listener.onPostExecute(comments);
	}

	@Override
	protected ArrayList<Entity> doInBackground(Entity... things) {
		try {
			URL commentsUrl = new URL("http://www.reddit.com/comments/" + things[0].getId() + ".json");
			Log.v(TAG, commentsUrl.toString());
			
			HttpURLConnection connection = (HttpURLConnection) commentsUrl.openConnection();
			connection.connect();
			
			long t1 = SystemClock.currentThreadTimeMillis();
			
			InputStream stream = connection.getInputStream();
			JsonReader reader = new JsonReader(new InputStreamReader(stream));
			EntityParser parser = new EntityParser();
			parser.parseListingArray(reader);
			stream.close();
			
			long t2 = SystemClock.currentThreadTimeMillis();
			Log.v(TAG, Long.toString(t2 - t1));
			Log.v(TAG, Integer.toString(parser.entities.size()));
			
			return parser.entities;
			
		} catch (MalformedURLException e) {
			Log.e(TAG, "", e);
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
		return null;
	}
	
	class EntityParser extends JsonParser {
		
		private final ArrayList<Entity> entities = new ArrayList<Entity>(360);
		
		@Override
		public void onEntityStart(int index) {
			entities.add(new Entity());
		}
		
		@Override
		public void onKind(JsonReader reader, int index) throws IOException {
			Entity e = entities.get(index);
			String kind = reader.nextString();
			if (index == 0) {
				e.type = Entity.TYPE_HEADER;
			} else if ("more".equalsIgnoreCase(kind)) {
				e.type = Entity.TYPE_MORE;
			} else {
				e.type = Entity.TYPE_COMMENT;
			}
			e.nesting = replyNesting;
		}
		
		@Override
		public void onTitle(JsonReader reader, int index) throws IOException {
			getEntity(index).title = getString(reader);
		}
		
		@Override
		public void onAuthor(JsonReader reader, int index) throws IOException {
			getEntity(index).author = getString(reader);
		}

		@Override
		public void onSelfText(JsonReader reader, int index) throws IOException {
			getEntity(index).selfText = getString(reader);
		}
		
		@Override
		public void onBody(JsonReader reader, int index) throws IOException {
			getEntity(index).body = getString(reader);
		}
		
		@Override
		public void onUps(JsonReader reader, int index) throws IOException {
			getEntity(index).ups = reader.nextInt();
		}
		
		@Override
		public void onDowns(JsonReader reader, int index) throws IOException {
			getEntity(index).downs = reader.nextInt();
		}
		
		private Entity getEntity(int index) {
			return entities.get(index);
		}
		
		private String getString(JsonReader reader) throws IOException {
			return reader.nextString().trim();
		}

		@Override
		public void onEntityEnd(int index) {
			Entity e = entities.get(index);
			switch (e.type) {
			case Entity.TYPE_HEADER:
				e.line1 = Formatter.formatTitle(e.title);
				e.line2 = Formatter.format(e.selfText);
				e.line3 = getStatus(e);
				break;
				
			case Entity.TYPE_COMMENT:
				e.line1 = Formatter.format(e.body);
				e.line2 = getStatus(e);
				break;
				
			case Entity.TYPE_MORE:
				e.line1 = new SpannedString(context.getString(R.string.load_more));
				e.progress = false;
				break;
			
			default:
				throw new IllegalArgumentException("Unsupported type: " + e.type);
			}
		}

		private SpannableStringBuilder getStatus(Entity e) {
			SpannableStringBuilder b = new SpannableStringBuilder();
			b.append(e.author);
			b.append("  ");
			int score = e.ups - e.downs;
			if (score > 0) {
				b.append("+");
			}
			b.append(Integer.toString(score));
			return b;
		}
		
		@Override
		public boolean parseReplies() {
			return true;
		}
	}
}
