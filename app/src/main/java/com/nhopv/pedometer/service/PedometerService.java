package com.nhopv.pedometer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.nhopv.pedometer.activity.MainActivity;

public class PedometerService extends Service implements SensorEventListener {
    private static final String TAG = "AAA";
    SensorManager sensorManager;

    private Boolean isRunning = false;
    private Float previousTotalSteps = 0f;
    int currentSteps;
    private double height;
    private double weight;
    private Float totalSteps;
    private int target;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Toast.makeText(this, "Không có cảm biến được phát hiện trên thiết bị này", Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
        loadData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        isRunning = true;
        height = intent.getDoubleExtra("height", 0);
        weight = intent.getDoubleExtra("weight", 0);
        target = intent.getIntExtra("target", 0);
        //flag này có tác dụng khi android bị kill hoặc bộ nhớ thấp, hệ thống sẽ start lại và gửi kết quả lần nữa.
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveDate();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning) {
            totalSteps = event.values[0];
            currentSteps = (int) (totalSteps - previousTotalSteps);

            double averageSpeed = 0.0008; // km/bước
            double calorieConsumption = Math.ceil(currentSteps * CountCaloriesFor1Step() * 100) / 100;
            double kmReach = Math.ceil(currentSteps * averageSpeed * 100) / 100;
            Log.d(TAG, "onSensorChanged: Steps = " + currentSteps + "\ntotalSteps: " + totalSteps + "\npreviousTotalSteps: " + previousTotalSteps);

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.mBroadcastAction);
            broadcastIntent.putExtra("steps", currentSteps);
            broadcastIntent.putExtra("calo", calorieConsumption);
            broadcastIntent.putExtra("km", kmReach);
            broadcastIntent.putExtra("target", target);
            sendBroadcast(broadcastIntent);
            loadData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void saveDate() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("key1", totalSteps);
        editor.putInt("target", target);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        Float saveNumber = sharedPreferences.getFloat("key1", 0f);
        Log.d(TAG, String.valueOf(saveNumber));
        previousTotalSteps = saveNumber;
    }

    private double CountCaloriesFor1Step() {
        double Calories;
        double averageSpeed = 5;
        double caloriesFor1Minute = (0.035 * weight) + ((averageSpeed * averageSpeed) / height) * 0.029 * weight * 100;
        Calories = caloriesFor1Minute / 100;
        return Calories;
    }
}
