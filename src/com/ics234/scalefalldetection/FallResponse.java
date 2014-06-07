package com.ics234.scalefalldetection;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.provider.Settings.Secure;

public class FallResponse extends Activity {
	private GradientTextView timer;
	private SoundPool sp;
	private int soundId;
	private double maxAccel;
	private CountDownTimer alertTimer;
	private MqttClient client;
	public static final String BROKER_URL = "tcp://dime.smartamerica.io:1883";

	public String TOPIC;
	private boolean isEncrypted = false;
	private String userName = "vbjsfwul";
	private String password = "xottyHH5j9v2";
	private SharedPreferences prefs;
	private int curVolume;
	private int curDuration;
	private int curTone;
	private int curVolumeIndex;
	private int curDurationIndex;
	private int curToneIndex;
	WakeLock wakeLock;
	private GradientTextView alertBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fall_response);

		stopService(new Intent(this, FallService.class));

		timer = (GradientTextView) findViewById(R.id.timer_count);
		
		prefs = this.getSharedPreferences(
			      "fall_detection", Context.MODE_PRIVATE);
		
		String volumeKey = "alarm_volume";
		curVolume = prefs.getInt(volumeKey, 4);
		
		String timerKey = "alarm_duration";
		curDuration = prefs.getInt(timerKey, 2);
		
		String toneKey = "alarm_tone";
		curTone = prefs.getInt(toneKey, 2);
		
		curVolumeIndex = findIndex(Constants.VOLUME_OPTIONS_VALS, curVolume);
		curDurationIndex = findIndex(Constants.TIMER_OPTIONS_VALS, curDuration);
		curToneIndex = findIndex(Constants.TONE_OPTIONS_VALS, curTone);
		
		
		timer.setStartColor(Color.argb(255, 16, 115, 115));
		timer.setFinishColor(Color.argb(255, 15, 113, 64));
		
		alertBtn = (GradientTextView) findViewById(R.id.fall_detected_text);
		alertBtn.setStartColor(Color.argb(200, 191, 255, 191));
		alertBtn.setFinishColor(Color.argb(200, 191, 255, 220));

		timerControl();

		sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

		/** soundId for Later handling of sound pool **/
		soundId = sp.load(getApplicationContext(), Constants.TONE_OPTIONS_VALS[curToneIndex], 1);

		try {
			client = new MqttClient(BROKER_URL, MqttClient.generateClientId(),
					null);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}

		maxAccel = getIntent().getExtras().getDouble("maxAccel");
		
		AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Constants.VOLUME_OPTIONS_VALS[curVolumeIndex], 0);
		

		String android_id = Secure.getString(this.getContentResolver(),
		                                                        Secure.ANDROID_ID);
		TOPIC  = "iot-1/d/" + android_id + "/evt/alert/json";
		
		KeyguardManager km = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
		final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
		kl.disableKeyguard();

		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
		        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
		wakeLock.acquire();
		

	}
	
	public int findIndex(int[] data, int val) {
		for (int i = 0; i < data.length; i++) {
			if (val == data[i])
				return i;
		}
		return -1;
	}

	public void timerControl() {
		alertTimer = new CountDownTimer(Constants.TIMER_OPTIONS_VALS[curDurationIndex], 1000) {

			public void onTick(long millisUntilFinished) {
				long secondsLeft = millisUntilFinished / 1000;
				int min;
				int sec;
				if (secondsLeft >= 60) {
					min = (int) secondsLeft / 60;
					sec = (int) secondsLeft - (min * 60);
				} else {
					sec = (int) secondsLeft;
					min = 0;
				}
				String tmp = String.format("%02d:%02d", min, sec);
				timer.setText(tmp);
				if (secondsLeft % 5 == 0) {
					sp.play(soundId, 1, 1, 0, 0, 1);
				}
				if (secondsLeft <= 10) {
					if (secondsLeft % 2 == 0)
						timer.setTextColor(Color.RED);
					else
						timer.setTextColor(Color.BLACK);
				}
			}

			public void onFinish() {
				send_fall();
				startService(new Intent(getApplicationContext(),
						FallService.class));
				Toast.makeText(getApplicationContext(),
						"Help has been contacted", Toast.LENGTH_SHORT).show();
				finish();
			}
		}.start();
	}

	public void cancelAlertMessage(View v) {
		Toast.makeText(this, "Alert has been cancelled", Toast.LENGTH_SHORT).show();
		if (alertTimer != null) {
			alertTimer.cancel();
			alertTimer = null;
		}
		startService(new Intent(getApplicationContext(), FallService.class));
	}
	
	public void sendAlertMessage(View v) {
		if (alertTimer != null) {
			alertTimer.cancel();
			alertTimer = null;
		}
		send_fall();
		startService(new Intent(getApplicationContext(),FallService.class));
		Toast.makeText(getApplicationContext(),
				"Help has been contacted", Toast.LENGTH_SHORT).show();
		finish();
	}

	private void send_fall() {

		MqttConnectOptions mqttOp = new MqttConnectOptions();
		if (isEncrypted) {
			mqttOp.setUserName(userName);
			mqttOp.setPassword(password.toCharArray());
		}
		

		try {
			if (isEncrypted)
				client.connect(mqttOp);
			else
				client.connect();

			publishFall();

			Log.i("Fall", "Sent fall mqtt");

			client.disconnect();

		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void publishFall() throws MqttException {

		JSONObject device = new JSONObject();
		JSONObject content = new JSONObject();
		JSONObject location = new JSONObject();
		JSONObject freeThresh = new JSONObject();
		JSONObject fallMessage = new JSONObject();
		JSONObject impactThresh = new JSONObject();

		try {
			content.put("event", "fall");
			device.put("id", "4100");
			device.put("type", "personfall");
			device.put("platform", "Android");
			device.put("version", "1.0");
			location.put("lat", "39.08301");
			location.put("lon", "-77.149629");
			content.put("location", location);
			content.put("device", device);
			freeThresh.put("value", "2");
			freeThresh.put("unit", "m/s^2");
			impactThresh.put("value", "25");
			impactThresh.put("unit", "m/s^2");
			content.put("cond_freefall_threshold_val", freeThresh);
			content.put("cond_impact_threshold_val", impactThresh);
			content.put("value", Double.toString(maxAccel));
			content.put("prio_class", "high");
			content.put("prio_value", 2);
			content.put("misc", "null");
			fallMessage.put("d", content);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		String jsonStr = fallMessage.toString();
		Log.i("FALL", "jsonString: " + jsonStr);

		final MqttTopic fallTopic = client.getTopic(TOPIC);

		final MqttMessage message = new MqttMessage(jsonStr.getBytes());
		fallTopic.publish(message);

	}
	
	public void onPause() {
		super.onPause();
		if (wakeLock.isHeld())
		    wakeLock.release();
	}
}
