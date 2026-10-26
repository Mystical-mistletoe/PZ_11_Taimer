package com.example.pz_11_taimer;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private EditText editTimerInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTimerInput = findViewById(R.id.editTimerInput);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    //запуск секундомера
    public void startStopwatch(View view) {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("START_STOPWATCH");
        startService(intent);
    }
    //таймер запуск
    public void startTimer(View view) {
        String input = editTimerInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Введите время в секундах", Toast.LENGTH_SHORT).show();
            return;
        }
        int seconds = Integer.parseInt(input);
        if (seconds <= 0) {
            Toast.makeText(this, "Время должно быть больше 0", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TimerService.class);
        intent.setAction("START_TIMER");
        intent.putExtra("extra_seconds", seconds); // передаём время
        startService(intent);
    }
    //остановка
    public void stopTimerService(View view) {
        Intent intent = new Intent(this, TimerService.class);
        stopService(intent);
    }
}