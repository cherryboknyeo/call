package com.example.photothis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 알림 생성
        createNotification(context);
    }

    private void createNotification(Context context) {
        String channelId = "diary_reminder_channel";
        String channelName = "Diary Reminder";

        // NotificationManager 설정
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // API 26 이상에서 NotificationChannel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 Intent 설정
        Intent notificationIntent = new Intent(context, MyPageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 알림 빌더 설정
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 설정 (drawable 폴더에 추가해야 함)
                .setContentTitle("일기 알림")
                .setContentText("일기 작성을 잊지 마세요!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // 알림 클릭 시 자동으로 제거

        // 알림 표시
        notificationManager.notify(1, builder.build());
    }
}
