package com.example.pz_11_taimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {
    private Handler updateHandler;
    private Runnable updateTask;
    private boolean isStopwatchMode = true;
    private int secondsPassed = 0;
    private int secondsLeft = 0;
    private boolean isTimerRinging = false;
    private MediaPlayer soundPlayer;
    private NotificationManager notificationHelper;
    private static final String NOTIFICATION_CHANNEL = "timer_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        updateHandler = new Handler(Looper.getMainLooper());
        notificationHelper = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        soundPlayer = MediaPlayer.create(this, R.raw.take_your_chance);
        if (soundPlayer != null) {
            soundPlayer.setLooping(true);
        }
        createNotificationChannelIfNeeded();
        updateTask = new Runnable() {
            @Override
            public void run() {
                if (isTimerRinging) return;

                if (isStopwatchMode) {
                    secondsPassed++;
                    showNotification("Секундомер", "Прошло: " + secondsPassed + " сек.");
                } else {
                    secondsLeft--;
                    if (secondsLeft <= 0) {
                        isTimerRinging = true;
                        playSound();
                        showToast("Таймер звонит!");
                        showNotification("Таймер звонит!", "Нажмите Стоп, чтобы выключить");
                        return;
                    }
                    showNotification("Таймер", "Осталось: " + secondsLeft + " сек.");
                }
                updateHandler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String command = intent.getAction();
            updateHandler.removeCallbacks(updateTask);
            stopSound();
            isTimerRinging = false;

            if (command.equals("START_TIMER")) {
                isStopwatchMode = false;
                secondsLeft = intent.getIntExtra("extra_seconds", 10);
            } else if (command.equals("START_STOPWATCH")) {
                isStopwatchMode = true;
                secondsPassed = 0;
            }

            startForeground(NOTIFICATION_ID,
                    buildNotification("Запуск...", "Подготовка..."));

            updateHandler.post(updateTask);
        }
        return START_STICKY;
    }

    private void playSound() {
        if (soundPlayer != null && !soundPlayer.isPlaying()) {
            soundPlayer.seekTo(0);
            soundPlayer.start();
        }
    }

    private void stopSound() {
        if (soundPlayer != null && soundPlayer.isPlaying()) {
            soundPlayer.pause();
            soundPlayer.seekTo(0);
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void showNotification(String title, String text) {
        notificationHelper.notify(NOTIFICATION_ID, buildNotification(title, text));
    }
    private Notification buildNotification(String title, String text) {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setOngoing(true) // нельзя убрать уведоление, пока работает
                .build();
    }
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationHelper.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateHandler.removeCallbacks(updateTask);
        stopSound();

        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }
        // уборка уведомления при явной остановке
        notificationHelper.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}