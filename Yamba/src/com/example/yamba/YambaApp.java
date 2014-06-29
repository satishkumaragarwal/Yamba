package com.example.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.TwitterException;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApp extends Application implements
		OnSharedPreferenceChangeListener {

	private Twitter jtwit;
	public static String TAG = "YambaApp";
	SharedPreferences prefs;
	StatusData statusData;
	static String REFRESH_STATUS = "com.example.yamba.REFRESH_STATUS";

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			prefs.registerOnSharedPreferenceChangeListener(this);
			statusData = new StatusData(this);

		} catch (Exception e) {
			Log.e(TAG, "Error in Application Object", e);
		}
	}

	public Twitter getJtwit() {
		if (this.jtwit == null) {
			Log.d(TAG, "After Shared Pref Update");
			String username = this.prefs.getString("username", "");
			String password = this.prefs.getString("password", "");
			String server = this.prefs.getString("server", "");
			jtwit = new Twitter(username, password);
			jtwit.setAPIRootUrl(server);
			Log.d(TAG, "Please Print" + username + password + server);
		}
		return jtwit;

	}
	
	long lastRecordedTime = -1;
	
	public int PullandInsert() {
		int count = 0;
		long lastTimestamp = lastRecordedTime;
		try {
			List<Status> refreshtimeline = getJtwit().getPublicTimeline();
			for (Status status : refreshtimeline) {
				if (status.createdAt.getTime() > lastTimestamp) {
					count++;
					Log.d("TwitterTimeline",
							String.format("%s: %s", status.user.name, status.text));
					Log.d(TAG,String.format("Count is: %d", status.createdAt.getTime()));
					lastTimestamp = status.createdAt.getTime();
				}
				statusData.insertData(status);
			}
			Log.d(TAG, String.format("Count is: %d", count));

		} catch (TwitterException e) {
			Log.d(TAG, "Get Public timeline method failed");
		}
		
		if(count > 0){
			sendBroadcast(new Intent(REFRESH_STATUS).putExtra("count", count));
		}
		lastRecordedTime = lastTimestamp;
		
		return count;

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		jtwit = null;
		Log.d(TAG, "Shared Pref Succeeded for key" + arg1);
	}

}
