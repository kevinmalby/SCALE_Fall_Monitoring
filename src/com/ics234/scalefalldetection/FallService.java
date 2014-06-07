package com.ics234.scalefalldetection;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.preference.PreferenceManager;

public class FallService extends Service implements SensorEventListener {

	public static final String TAG = FallService.class.getName();
	public static final int SCREEN_OFF_RECEIVER_DELAY = 500;
	public static final int LOW_THRESH = 2;
	public static final int HIGH_THRESH = 25;
	private static long lastTime = 0;
	private static float innerTimer = 0;
	private static float outerTimer = 0;
	private double[] accelVals = new double[5];
	private boolean timeFreeFallEvent = false;
	private boolean timeGroundContactEvent = false;
	private boolean fallPhaseOne = false;
	private boolean fallPhaseTwo = false;
	private boolean fallOccurred = false;
	private double maxAccelExperienced = 0.0;

	private SensorManager mSensorManager = null;
	private WakeLock mWakeLock = null;

	private SharedPreferences prefs;

	/*
	 * Register this as a sensor event listener.
	 */
	private void registerListener() {
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	/*
	 * Un-register this as a sensor event listener.
	 */
	private void unregisterListener() {
		mSensorManager.unregisterListener(this);
	}

	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				return;
			}

			Runnable runnable = new Runnable() {
				public void run() {
					unregisterListener();
					registerListener();
				}
			};

			new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
		}
	};

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long temp;
			temp = event.timestamp;

			// Calculate the acceleration and store in the buffer
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			double accelAvg = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)
					+ Math.pow(z, 2));
			shiftBuffer(accelAvg);


			if (fallPhaseOne) {
				if (accelAvg > maxAccelExperienced)
					maxAccelExperienced = accelAvg;
			}

			// Check what the current fall event is and take appropriate action
			int fallEvent = checkBufferEvent(accelAvg);
			switch (fallEvent) {
			case -1:
				break;
			case 0:
				if (timeFreeFallEvent == false) {
					timeFreeFallEvent = true;
					innerTimer = 0;
					outerTimer = 0;
				}
				break;
			case 1:
				if (timeGroundContactEvent == false) {
					timeGroundContactEvent = true;
				}
				break;
			}

			// Time the free fall and make sure it's long enough for a real fall
			if (timeFreeFallEvent == true) {
				if (innerTimer >= (prefs.getFloat("free_fall_slider", (float)0.2)*4) && getBufferMean() < (prefs.getFloat("low_threshold_slider", 2))*3) {
					fallPhaseOne = true;
					timeFreeFallEvent = false;
				} else if (innerTimer < 0.15 && getBufferMean() > (prefs.getFloat("low_threshold_slider", 2))*3)
					timeFreeFallEvent = false;
			}

			// Time the ground contact and make sure it's long enough for a real
			// fall
			if (timeGroundContactEvent == true) {
				fallPhaseTwo = true;
				timeGroundContactEvent = false;
			}

			// Add time elapsed
			innerTimer += ((float) (temp - lastTime) / 1000000000.0);
			outerTimer += ((float) (temp - lastTime) / 1000000000.0);


			double fallDur = (prefs.getFloat("fall_duration_slider", 4)*8);
			if (outerTimer <= fallDur && fallPhaseOne == true && fallPhaseTwo == true)
				fallOccurred = true;
			else if (outerTimer > fallDur) {
				fallPhaseOne = false;
				timeFreeFallEvent = false;
				timeGroundContactEvent = false;
				for (int i = 0; i < accelVals.length; i++)
					accelVals[i] = 9.81;
				outerTimer = 0;
			}

			if (fallOccurred == true) {
				fallOccurred = false;
				timeFreeFallEvent = false;
				timeGroundContactEvent = false;
				fallPhaseOne = false;
				fallPhaseTwo = false;

				for (int i = 0; i < accelVals.length; i++)
					accelVals[i] = 9.81;

				// Launch the activity that will create the timer and handle
				// emergency response
				Intent intent = new Intent(getBaseContext(), FallResponse.class);
				intent.putExtra("maxAccel", maxAccelExperienced);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplication().startActivity(intent);

				maxAccelExperienced = 0.0;
			}

			lastTime = event.timestamp;
		}
	}

	public void shiftBuffer(double accelData) {
		for (int i = accelVals.length - 1; i > 0; i--)
			accelVals[i] = accelVals[i - 1];

		accelVals[0] = accelData;
	}

	public int checkBufferEvent(double accelData) {

		if (accelData < (prefs.getFloat("low_threshold_slider", 2))*3) {
			return 0;
		} else if (fallPhaseOne == true && accelData > (prefs.getFloat("high_threshold_slider", 20))*30) {
			return 1;
		} else {
			return -1;
		}
	}

	public double getBufferMean() {
		double sum = 0.0;
		for (double d : accelVals)
			sum += d;
		double mean = sum / accelVals.length;
		return mean;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

		registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		outerTimer = 0;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		unregisterListener();
		mWakeLock.release();
		stopForeground(true);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		startForeground(Process.myPid(), new Notification());
		registerListener();
		mWakeLock.acquire();

		return START_STICKY;
	}

}
