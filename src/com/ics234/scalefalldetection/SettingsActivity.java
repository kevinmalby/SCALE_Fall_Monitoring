package com.ics234.scalefalldetection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final int LOW_THRESH = 2;
	public static final int HIGH_THRESH = 25;
	private SharedPreferences pref;
	private SharedPreferences beg;

	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.my_preferences);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(this);
		
		SliderPreference ffd = (SliderPreference) getPreferenceManager().findPreference("free_fall_slider");
		SliderPreference fds = (SliderPreference) getPreferenceManager().findPreference("fall_duration_slider");
		SliderPreference lts = (SliderPreference) getPreferenceManager().findPreference("low_threshold_slider");
		SliderPreference hts = (SliderPreference) getPreferenceManager().findPreference("high_threshold_slider");
		ffd.setRange(4);
		fds.setRange(8);
		lts.setRange(3);
		hts.setRange(30);
		
		beg = this.getSharedPreferences("fall_detection",
				Context.MODE_PRIVATE);

		if (beg.getBoolean("beginning_entry", true)) {

			pref.edit().putFloat("free_fall_slider", (float) 0.05).commit(); 
		    pref.edit().putFloat("fall_duration_slider", (float) 0.5).commit(); 
		    pref.edit().putFloat("low_threshold_slider", (float) 0.67).commit(); 
		    pref.edit().putFloat("high_threshold_slider", (float) 0.8).commit();
		    beg.edit().putBoolean("beginning_entry", false).commit();
		}
		
		String sliderSum;
		double sliderVal = pref.getFloat("free_fall_slider", (float) 0.05);
		sliderSum = String.format("Current Duration: %1.2f s", sliderVal*4);
		ffd.setSummary(sliderSum);
		
		sliderVal = pref.getFloat("fall_duration_slider", (float) 0.5);
		sliderSum = String.format("Current Duration: %1.2f s", sliderVal*8);
		fds.setSummary(sliderSum);
		
		sliderVal = pref.getFloat("low_threshold_slider", (float) 0.67);
		sliderSum = String.format("Current Threshold: %1.2f m/s^2", sliderVal*3);
		lts.setSummary(sliderSum);
		
		sliderVal = pref.getFloat("high_threshold_slider", (float) 0.8);
		sliderSum = String.format("Current Threshold: %1.2f m/s^2", sliderVal*30);
		hts.setSummary(sliderSum);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		float mZoomValue = sharedPreferences.getFloat(key, (float) 0.5);
		
		if (key.equals("free_fall_slider")) {
			Log.i("FALL", "Free fall: " + Float.toString(mZoomValue));
			SliderPreference ffd = (SliderPreference) getPreferenceManager().findPreference("free_fall_slider");
			String sliderSum;
			double sliderVal = pref.getFloat("free_fall_slider", (float) 0.05);
			sliderSum = String.format("Current Duration: %1.2f s", sliderVal*4);
			ffd.setSummary(sliderSum);
		} else if (key.equals("fall_duration_slider")) {
			Log.i("FALL", "Free duration: " + Float.toString(mZoomValue));
			SliderPreference fds = (SliderPreference) getPreferenceManager().findPreference("fall_duration_slider");
			String sliderSum;
			double sliderVal = pref.getFloat("fall_duration_slider", (float) 0.5);
			sliderSum = String.format("Current Duration: %1.2f s", sliderVal*8);
			fds.setSummary(sliderSum);
		} else if (key.equals("low_threshold_slider")) {
			Log.i("FALL", "low thresh: " + Float.toString(mZoomValue));
			SliderPreference lts = (SliderPreference) getPreferenceManager().findPreference("low_threshold_slider");
			String sliderSum;
			double sliderVal = pref.getFloat("low_threshold_slider", (float) 0.67);
			sliderSum = String.format("Current Duration: %1.2f m/s^2", sliderVal*3);
			lts.setSummary(sliderSum);
		} else if (key.equals("high_threshold_slider")) {
			Log.i("FALL", "high thresh: " + Float.toString(mZoomValue));
			SliderPreference hts = (SliderPreference) getPreferenceManager().findPreference("high_threshold_slider");
			String sliderSum;
			double sliderVal = pref.getFloat("high_threshold_slider", (float) 0.8);
			sliderSum = String.format("Current Duration: %1.2f m/s^2", sliderVal*30);
			hts.setSummary(sliderSum);
		}

	}
}
