package com.example.literise.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

import com.example.literise.R;
import com.example.literise.activities.DashboardActivity;
import com.example.literise.activities.SettingsActivity;

/**
 * BroadcastReceiver that fires the daily "time to practice" push notification.
 * Scheduled by SettingsActivity at 9:00 AM every day when reminders are enabled.
 */
public class DailyReminderReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check that reminders are still enabled in prefs
        SharedPreferences prefs = context.getSharedPreferences(
                SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(SettingsActivity.KEY_REMINDERS_ENABLED, true)) {
            return;
        }

        // Build the notification
        Intent openApp = new Intent(context, DashboardActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, SettingsActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_star)
                .setContentTitle("Time to practice, LiteRiser! 🦁")
                .setContentText("Keep your streak alive — complete a lesson today!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Keep your streak alive — complete a lesson today! "
                                + "Even 5 minutes of practice makes a big difference."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, builder.build());
        }

        // Re-schedule for the next day (needed on API 23+ with setExactAndAllowWhileIdle)
        rescheduleForTomorrow(context);
    }

    private void rescheduleForTomorrow(Context context) {
        android.app.AlarmManager alarmManager =
                (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, DailyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 9);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
