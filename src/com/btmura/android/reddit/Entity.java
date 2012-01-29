package com.btmura.android.reddit;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

public class Entity implements Parcelable {
	
	public static final int NUM_TYPES = 4;
	public static final int TYPE_TITLE = 0;
	public static final int TYPE_HEADER = 1;
	public static final int TYPE_COMMENT = 2;
	public static final int TYPE_MORE = 3;
	
	public int type;
	public String name;
	public String title;
	public String author;
	public String url;
	public boolean isSelf;
	public String selfText;
	public String body;	
	
	public Spanned line1;
	public Spanned line2;
	public Spanned line3;
	public int nesting;
	
	public Entity() {
	}
	
	public static final Parcelable.Creator<Entity> CREATOR = new Parcelable.Creator<Entity>() {
		public Entity createFromParcel(Parcel source) {
			return new Entity(source);
		}
		
		public Entity[] newArray(int size) {
			return new Entity[size];
		}
	};
	
	Entity(Parcel parcel) {
		type = parcel.readInt();
		name = parcel.readString();
		title = parcel.readString();
		author = parcel.readString();
		url = parcel.readString();
		isSelf = parcel.readInt() == 1;
		selfText = parcel.readString();
		body = parcel.readString();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(name);
		dest.writeString(title);
		dest.writeString(author);
		dest.writeString(url);
		dest.writeInt(isSelf ? 1 : 0);
		dest.writeString(selfText);
		dest.writeString(body);
	}
	
	public String getId() {
		int sepIndex = name.indexOf('_');
		return name.substring(sepIndex + 1);
	}

	public int describeContents() {
		return 0;
	}
}
