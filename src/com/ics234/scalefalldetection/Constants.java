package com.ics234.scalefalldetection;

public class Constants {

	public static final String[] VOLUME_OPTIONS = 
		{"VERY LOW", "LOW", "MEDIUM", "HIGH", "VERY HIGH"};
	
	public static final int[] VOLUME_OPTIONS_VALS = 
		{1, 3, 6, 8, 11};
	
	public static final String[] TIMER_OPTIONS = 
		{"00:15s", "00:30s", "00:45s", "01:00s", "01:15s"};
	
	public static final int[] TIMER_OPTIONS_VALS = 
		{15000, 30000, 45000, 60000, 75000};
	
	public static final String[] TONE_OPTIONS =
		{"CLOCK", "SYMPHONY", "BASIC", "SHIP BELL", "SIREN"};
	
	public static final int[] TONE_OPTIONS_VALS = 
		{R.raw.alarm_clock, R.raw.orchestra, R.raw.basic,
		R.raw.ship_bell, R.raw.warning_siren};
}
