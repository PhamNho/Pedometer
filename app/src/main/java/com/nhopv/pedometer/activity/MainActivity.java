package com.nhopv.pedometer.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.nhopv.pedometer.service.PedometerService;
import com.nhopv.pedometer.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AAA";
    private EditText edtHeight;
    private EditText edtWeight;
    private EditText edtTarget;
    private CircularProgressBar circularProgressBar;
    private TextView tvSteps;
    private TextView tvProgress;
    private TextView tvCalories;
    private TextView tvKilometers;
    private int steps;
    private int target;
    private double calo, km;

    public static final String mBroadcastAction = "STRING_BROADCAST_ACTION";
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadData();
        Intent intent = new Intent(MainActivity.this, PedometerService.class);
        tvProgress.setText("/" + 3000);
        circularProgressBar.setProgressMax(3000f);
        intent.putExtra("height", 170.0);
        intent.putExtra("weight", 70.0);
        intent.putExtra("target", 3000);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    private void initView() {
        edtHeight = findViewById(R.id.edtHeight);
        edtWeight = findViewById(R.id.edtWeight);
        edtTarget = findViewById(R.id.edtTarget);
        circularProgressBar = findViewById(R.id.circularProgressBar);
        tvSteps = findViewById(R.id.tvSteps);
        tvProgress = findViewById(R.id.tvProgress);
        tvCalories = findViewById(R.id.tvCalories);
        tvKilometers = findViewById(R.id.tvKilometers);
        mIntentFilter = new IntentFilter();
        // Đăng ký action cho IntentFilter
        mIntentFilter.addAction(mBroadcastAction);

        // Gán giá trị mặc định cho form nhập
        edtHeight.setText("170");
        edtWeight.setText("68");
        edtTarget.setText("3000");
    }

    // Tạo một BroadcastReceiver lắng nghe service
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastAction)) {
                Log.d(TAG, "steps: " + steps + "--calo :" + calo + "--km: " + km);
                steps = intent.getIntExtra("steps", 0);
                calo = intent.getDoubleExtra("calo", 0);
                km = intent.getDoubleExtra("km", 0);
                target = intent.getIntExtra("target", 0);
                tvSteps.setText(String.valueOf(steps));
                tvCalories.setText(String.valueOf(calo));
                tvKilometers.setText(String.valueOf(km));
                tvProgress.setText("/" + target);
                circularProgressBar.setProgressMax((float) target);
                circularProgressBar.setProgressWithAnimation(steps);
                checkProgress(steps, target);
            }
        }
    };

    @SuppressLint("SetTextI18n")
    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, PedometerService.class);
        switch (view.getId()) {
            case R.id.btnStart:
//                String mHeight = edtHeight.getText().toString().trim();
//                String mWeight = edtWeight.getText().toString().trim();
//                String mTarget = edtTarget.getText().toString().trim();
//                if (mHeight.isEmpty()) {
//                    edtHeight.setError(getText(R.string.notify_height_empty));
//                } else if (Integer.parseInt(mHeight) > 300) {
//                    edtHeight.setError(getText(R.string.notify_height_smaller_300cm));
//                } else if (mWeight.isEmpty()) {
//                    edtWeight.setError("Bạn chưa nhập cân nặng (kg)");
//                } else if (mTarget.isEmpty()) {
//                    edtTarget.setError("Bạn chưa nhập mục tiêu (bước)");
//                } else {
//                    target = Integer.parseInt(mTarget);
//                    tvProgress.setText("/" + mTarget);
//                    circularProgressBar.setProgressMax(Float.parseFloat(mTarget));
//                    intent.putExtra("height", Double.parseDouble(mHeight));
//                    intent.putExtra("weight", Double.parseDouble(mWeight));
//                    intent.putExtra("target", target);
//                    startService(intent);
//                }
                break;
            case R.id.btnStop:
                stopService(intent);
                // clear Result
                tvProgress.setText(String.valueOf(0));
                tvCalories.setText(String.valueOf(0));
                tvKilometers.setText(String.valueOf(0));
                tvSteps.setText(String.valueOf(0));
                circularProgressBar.setProgressMax(0);
                circularProgressBar.setProgressWithAnimation(0);
                resetData();
                Toast.makeText(this, "Kết thúc Service", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int target = sharedPreferences.getInt("target", 0);
        int currentSteps = sharedPreferences.getInt("currentSteps", 0);
        String calorie = sharedPreferences.getString("calorie", "0");
        String kmReach = sharedPreferences.getString("kmReach", "0");

        Log.d(TAG, "target: " + target);
        tvProgress.setText("/" + target);
        tvKilometers.setText(kmReach);
        tvCalories.setText(calorie);
        tvSteps.setText(currentSteps + "");
        circularProgressBar.setProgress(currentSteps);
        circularProgressBar.setProgressMax((float) target);
    }
    private void resetData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("target", target);
        editor.putInt("currentSteps", 0);
        editor.putString("calorie", String.valueOf(0));
        editor.putString("kmReach", String.valueOf(0));
        editor.apply();
    }


    private void checkProgress(int steps, int target) {
        if (steps == target) {
            Intent intent = new Intent(MainActivity.this, PedometerService.class);
            stopService(intent);
            Toast.makeText(this, "Bạn đã hoàn thành mục tiêu \nHãy bắt đầu với mục tiêu mới", Toast.LENGTH_SHORT).show();
        }
    }

}
