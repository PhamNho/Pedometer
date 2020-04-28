package com.nhopv.pedometer1;

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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "NNN";
    private EditText edtHeight;
    private EditText edtWeight;
    private EditText edtTarget;
    private CircularProgressBar circularProgressBar;
    private TextView tvSteps;
    private TextView tvProgress;
    private TextView tvCalories;
    private TextView tvKilometers;
    private Float totalSteps = 0f;
    private Float previousTotalSteps = 0f;
    private int steps;
    private double calo, km;

    public static final String mBroadcastAction = "STRING_BROADCAST_ACTION";
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        resetSteps();
//        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //Tạo một BroadcastReceiver lắng nghe service
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastAction)) {
                Log.d(TAG, "steps: " + steps + "\n" + "calo :" + calo + "\n" + "km: " + km);
                steps = intent.getIntExtra("steps", 0);
                calo = intent.getDoubleExtra("calo", 0);
                km = intent.getDoubleExtra("km", 0);
                totalSteps = intent.getFloatExtra("totalSteps", 0);

                tvSteps.setText(String.valueOf(steps));
                tvCalories.setText(String.valueOf(calo));
                tvKilometers.setText(String.valueOf(km));
                circularProgressBar.setProgressWithAnimation(steps);
            }
        }
    };

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
        mIntentFilter.addAction(mBroadcastAction);
    }

    @SuppressLint("SetTextI18n")
    public void startStepCounter(View view) {
        String mHeight = edtHeight.getText().toString().trim();
        String mWeight = edtWeight.getText().toString().trim();
        String mTarget = edtTarget.getText().toString().trim();

        if (mHeight.isEmpty()) {
            edtHeight.setError("Bạn chưa nhập chiều cao (cm)");
        } else if (mWeight.isEmpty()) {
            edtWeight.setError("Bạn chưa nhập cân nặng (kg)");
        } else if (mTarget.isEmpty()) {
            edtTarget.setError("Bạn chưa nhập mục tiêu (bước)");
        } else {
            tvProgress.setText("/" + mTarget);
            circularProgressBar.setProgressMax(Float.parseFloat(mTarget));
            Intent intent = new Intent(MainActivity.this, PedometerService.class);
            intent.putExtra("height", Double.parseDouble(mHeight));
            intent.putExtra("weight", Double.parseDouble(mWeight));
            startService(intent);
        }
    }

//    void resetSteps() {
//        tvSteps.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Giữ để làm mới", Toast.LENGTH_SHORT).show();
//            }
//        });
//        tvSteps.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                previousTotalSteps = totalSteps;
//                tvSteps.setText(String.valueOf(0));
//                tvCalories.setText(String.valueOf(0));
//                tvKilometers.setText(String.valueOf(0));
//                circularProgressBar.setProgressWithAnimation(0);
//                saveDate();
//                Toast.makeText(MainActivity.this, "Đã làm mới", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//    }

//    private void saveDate() {
//        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putFloat("key1", previousTotalSteps);
//        editor.apply();
//    }

//    private void loadData() {
//        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
//        Float saveNumber = sharedPreferences.getFloat("key1", 0f);
//        Log.d(TAG, String.valueOf(saveNumber));
//        previousTotalSteps = saveNumber;
//    }
}
