package com.ics234.scalefalldetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

	private GradientTextView gTopHeader;
	private GradientTextView gSubHeader;
	private int curVolume;
	private int curDuration;
	private int curTone;
	private int curVolumeIndex;
	private int curTimerIndex;
	private int curToneIndex;
	private SharedPreferences prefs;
	private String toneKey = "alarm_tone";
	private String timerKey = "alarm_duration";
	private String volumeKey = "alarm_volume";
	private TextView volumeControlText;
	private TextView timerControlText;
	private TextView toneControlText;
	private boolean serviceActive;
	private String serviceActiveKey = "service_state";
	private ImageButton srv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		gTopHeader = (GradientTextView) findViewById(R.id.top_header);
		gTopHeader.setStartColor(Color.argb(255, 16, 115, 115));
		gTopHeader.setFinishColor(Color.argb(255, 15, 113, 64));

		gSubHeader = (GradientTextView) findViewById(R.id.sub_header);
		gSubHeader.setStartColor(Color.argb(255, 16, 115, 115));
		gSubHeader.setFinishColor(Color.argb(255, 15, 113, 64));

		prefs = this.getSharedPreferences("fall_detection",
				Context.MODE_PRIVATE);
		
		serviceActive = prefs.getBoolean(serviceActiveKey, false);
		
		if (!serviceActive)
		{
			prefs.edit().putBoolean(serviceActiveKey, false).commit();
		}
		
		srv = (ImageButton) findViewById(R.id.service_button);
		if (serviceActive)
		{
			srv.setImageDrawable(getResources().getDrawable( R.drawable.service_action_started ));
		} else {
			srv.setImageDrawable(getResources().getDrawable( R.drawable.service_action_nstarted ));
		}

		curVolume = prefs.getInt(volumeKey, -1);
		if (curVolume == -1) {
			curVolumeIndex = 4;
			prefs.edit()
			.putInt(volumeKey,
					Constants.VOLUME_OPTIONS_VALS[curVolumeIndex]).commit();
		} else {
			curVolumeIndex = findIndex(Constants.VOLUME_OPTIONS_VALS, curVolume);
		}
		volumeControlText = (TextView) findViewById(R.id.volume_control_text);
		volumeControlText.setText(Constants.VOLUME_OPTIONS[curVolumeIndex]);

		curDuration = prefs.getInt(timerKey, -1);
		if (curDuration == -1) {
			curTimerIndex = 2;
			prefs.edit()
			.putInt(timerKey,
					Constants.TIMER_OPTIONS_VALS[curTimerIndex]).commit();
		} else {
			curTimerIndex = findIndex(Constants.TIMER_OPTIONS_VALS, curDuration);
		}
		timerControlText = (TextView) findViewById(R.id.timer_control_text);
		timerControlText.setText(Constants.TIMER_OPTIONS[curTimerIndex]);

		curTone = prefs.getInt(toneKey, -1);
		if (curTone == -1) {
			curToneIndex = 2;
			prefs.edit()
			.putInt(toneKey,
					Constants.TONE_OPTIONS_VALS[curToneIndex]).commit();
		} else {
			curToneIndex = findIndex(Constants.TONE_OPTIONS_VALS, curTone);
		}
		toneControlText = (TextView) findViewById(R.id.tone_control_text);
		toneControlText.setText(Constants.TONE_OPTIONS[curToneIndex]);
	}

	public int findIndex(int[] data, int val) {
		for (int i = 0; i < data.length; i++) {
			if (val == data[i])
				return i;
		}
		return -1;
	}

	public void clickLeftNavVolume(View view) {
		curVolume = prefs.getInt(volumeKey, -1);
		curVolumeIndex = findIndex(Constants.VOLUME_OPTIONS_VALS, curVolume);

		if (curVolumeIndex == 0) {
			curVolumeIndex = Constants.VOLUME_OPTIONS_VALS.length - 1;
		} else {
			curVolumeIndex--;
		}
		prefs.edit()
				.putInt(volumeKey,
						Constants.VOLUME_OPTIONS_VALS[curVolumeIndex]).commit();
		volumeControlText = (TextView) findViewById(R.id.volume_control_text);
		volumeControlText.setText(Constants.VOLUME_OPTIONS[curVolumeIndex]);
	}

	public void clickRightNavVolume(View view) {
		curVolume = prefs.getInt(volumeKey, -1);
		curVolumeIndex = findIndex(Constants.VOLUME_OPTIONS_VALS, curVolume);

		if (curVolumeIndex == Constants.VOLUME_OPTIONS_VALS.length - 1) {
			curVolumeIndex = 0;
		} else {
			curVolumeIndex++;
		}
		prefs.edit()
				.putInt(volumeKey,
						Constants.VOLUME_OPTIONS_VALS[curVolumeIndex]).commit();
		volumeControlText = (TextView) findViewById(R.id.volume_control_text);
		volumeControlText.setText(Constants.VOLUME_OPTIONS[curVolumeIndex]);
	}

	public void clickLeftNavTimer(View view) {
		curDuration = prefs.getInt(timerKey, -1);
		curTimerIndex = findIndex(Constants.TIMER_OPTIONS_VALS, curDuration);

		if (curTimerIndex == 0) {
			curTimerIndex = Constants.TIMER_OPTIONS_VALS.length - 1;
		} else {
			curTimerIndex--;
		}
		prefs.edit()
				.putInt(timerKey, Constants.TIMER_OPTIONS_VALS[curTimerIndex])
				.commit();
		timerControlText = (TextView) findViewById(R.id.timer_control_text);
		timerControlText.setText(Constants.TIMER_OPTIONS[curTimerIndex]);
	}

	public void clickRightNavTimer(View view) {
		curDuration = prefs.getInt(timerKey, -1);
		curTimerIndex = findIndex(Constants.TIMER_OPTIONS_VALS, curDuration);

		if (curTimerIndex == Constants.TIMER_OPTIONS_VALS.length - 1) {
			curTimerIndex = 0;
		} else {
			curTimerIndex++;
		}
		prefs.edit()
				.putInt(timerKey, Constants.TIMER_OPTIONS_VALS[curTimerIndex])
				.commit();
		timerControlText = (TextView) findViewById(R.id.timer_control_text);
		timerControlText.setText(Constants.TIMER_OPTIONS[curTimerIndex]);
	}

	public void clickLeftNavTone(View view) {
		curTone = prefs.getInt(toneKey, -1);
		curTimerIndex = findIndex(Constants.TONE_OPTIONS_VALS, curTone);

		if (curToneIndex == 0) {
			curToneIndex = Constants.TONE_OPTIONS_VALS.length - 1;
		} else {
			curToneIndex--;
		}
		prefs.edit().putInt(toneKey, Constants.TONE_OPTIONS_VALS[curToneIndex])
				.commit();
		toneControlText = (TextView) findViewById(R.id.tone_control_text);
		toneControlText.setText(Constants.TONE_OPTIONS[curToneIndex]);
	}

	public void clickRightNavTone(View view) {
		curTone = prefs.getInt(toneKey, -1);
		curTimerIndex = findIndex(Constants.TONE_OPTIONS_VALS, curTone);

		if (curToneIndex == Constants.TONE_OPTIONS_VALS.length - 1) {
			curToneIndex = 0;
		} else {
			curToneIndex++;
		}
		prefs.edit().putInt(toneKey, Constants.TONE_OPTIONS_VALS[curToneIndex])
				.commit();
		toneControlText = (TextView) findViewById(R.id.tone_control_text);
		toneControlText.setText(Constants.TONE_OPTIONS[curToneIndex]);
	}

	public void onServiceToggle(View view) {
		
		serviceActive = prefs.getBoolean(serviceActiveKey, false);
		srv = (ImageButton) findViewById(R.id.service_button);
		if (serviceActive)
		{
			srv.setImageDrawable(getResources().getDrawable( R.drawable.service_action_nstarted ));
			prefs.edit().putBoolean(serviceActiveKey, false).commit();
			Intent intent = new Intent(this, FallService.class);
			stopService(intent);
		} else {
			srv.setImageDrawable(getResources().getDrawable( R.drawable.service_action_started ));
			prefs.edit().putBoolean(serviceActiveKey, true).commit();
			Intent intent = new Intent(this, FallService.class);
			startService(intent);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
