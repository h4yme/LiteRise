package com.example.literise.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.receivers.DailyReminderReceiver;
import com.example.literise.utils.MusicManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME     = "LiteRiseSettings";
    public static final String KEY_VOICE_NORMAL  = "voice_normal";
    public static final String KEY_SOUNDS_ENABLED = "sounds_enabled";
    public static final String KEY_MUSIC_ENABLED  = "music_enabled";
    public static final String KEY_REMINDERS_ENABLED = "reminders_enabled";

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    public static final String NOTIFICATION_CHANNEL_ID = "literise_reminders";

    private ImageView btnBack;
    private TextView  btnVoiceNormal, btnVoiceSlow;
    private SwitchMaterial switchSounds, switchMusic, switchReminders;
    private MaterialCardView cardAboutApp, cardChangeName, cardChangePassword;
    private TextView tvCurrentDisplayName;
    private TextView btnLogOut;

    private SharedPreferences prefs;
    private SessionManager    session;
    private boolean isVoiceNormal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = new SessionManager(this);
        prefs   = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        createNotificationChannel();
        initViews();
        loadSettings();
        setupListeners();
    }

    // ── Views ────────────────────────────────────────────────────────────────

    private void initViews() {
        btnBack              = findViewById(R.id.btnBack);
        btnVoiceNormal       = findViewById(R.id.btnVoiceNormal);
        btnVoiceSlow         = findViewById(R.id.btnVoiceSlow);
        switchSounds         = findViewById(R.id.switchSounds);
        switchMusic          = findViewById(R.id.switchMusic);
        switchReminders      = findViewById(R.id.switchReminders);
        cardAboutApp         = findViewById(R.id.cardAboutApp);
        cardChangeName       = findViewById(R.id.cardChangeName);
        cardChangePassword   = findViewById(R.id.cardChangePassword);
        tvCurrentDisplayName = findViewById(R.id.tvCurrentDisplayName);
        btnLogOut            = findViewById(R.id.btnLogOut);
    }

    // ── Load settings ────────────────────────────────────────────────────────

    private void loadSettings() {
        // Voice speed
        isVoiceNormal = prefs.getBoolean(KEY_VOICE_NORMAL, true);
        updateVoiceSpeedUI();

        // Sound effects
        switchSounds.setChecked(prefs.getBoolean(KEY_SOUNDS_ENABLED, true));

        // Background music
        switchMusic.setChecked(prefs.getBoolean(KEY_MUSIC_ENABLED, true));

        // Daily reminders
        switchReminders.setChecked(prefs.getBoolean(KEY_REMINDERS_ENABLED, true));

        // Display name sub-label
        String nick = session.getNickname();
        if (nick == null || nick.isEmpty()) nick = session.getFullname();
        tvCurrentDisplayName.setText((nick != null && !nick.isEmpty()) ? nick : "Tap to set");
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Voice speed
        btnVoiceNormal.setOnClickListener(v -> setVoiceSpeed(true));
        btnVoiceSlow.setOnClickListener(v -> setVoiceSpeed(false));

        // Sound effects
        switchSounds.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_SOUNDS_ENABLED, checked).apply();
            // Mirror to SoundPrefs so SoundEffectsHelper picks it up
            getSharedPreferences("SoundPrefs", MODE_PRIVATE)
                    .edit().putBoolean("sound_enabled", checked).apply();
        });

        // Background music
        switchMusic.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, checked).apply();
            MusicManager.getInstance(this).setMusicEnabled(checked);
        });

        // Daily reminders
        switchReminders.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, checked).apply();
            if (checked) {
                requestNotificationPermissionAndSchedule();
            } else {
                cancelDailyReminder();
            }
        });

        // Account
        cardChangeName.setOnClickListener(v -> showChangeNameDialog());
        cardChangePassword.setOnClickListener(v -> openChangePassword());

        // About
        cardAboutApp.setOnClickListener(v -> showAboutDialog());

        // Log out
        btnLogOut.setOnClickListener(v -> showLogoutDialog());
    }

    // ── Voice speed ──────────────────────────────────────────────────────────

    private void setVoiceSpeed(boolean normal) {
        isVoiceNormal = normal;
        prefs.edit().putBoolean(KEY_VOICE_NORMAL, normal).apply();
        updateVoiceSpeedUI();
    }

    private void updateVoiceSpeedUI() {
        if (isVoiceNormal) {
            btnVoiceNormal.setBackgroundResource(R.drawable.bg_voice_speed_selected);
            btnVoiceNormal.setTextColor(0xFFFFFFFF);
            btnVoiceSlow.setBackgroundResource(android.R.color.transparent);
            btnVoiceSlow.setTextColor(0xFF636E72);
        } else {
            btnVoiceSlow.setBackgroundResource(R.drawable.bg_voice_speed_selected);
            btnVoiceSlow.setTextColor(0xFFFFFFFF);
            btnVoiceNormal.setBackgroundResource(android.R.color.transparent);
            btnVoiceNormal.setTextColor(0xFF636E72);
        }
    }

    // ── Change display name ──────────────────────────────────────────────────

    private void showChangeNameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Enter display name");
        String current = session.getNickname();
        if (current != null && !current.isEmpty()) input.setText(current);
        input.setSelection(input.getText().length());
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad, pad, pad);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Display Name")
                .setMessage("This name appears on your profile and dashboard.")
                .setView(input)
                .setPositiveButton("Save", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        session.saveNickname(name);
                        tvCurrentDisplayName.setText(name);
                        android.widget.Toast.makeText(this,
                                "Display name updated!", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Change password ──────────────────────────────────────────────────────

    private void openChangePassword() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Change Password")
                .setMessage("We'll send a password reset link to your registered email address.\n\n"
                        + session.getEmail())
                .setPositiveButton("Send Reset Link", (d, w) -> {
                    Intent intent = new Intent(this, ForgotPasswordActivity.class);
                    intent.putExtra("prefill_email", session.getEmail());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Notifications ────────────────────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Daily Learning Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Reminds you to practice English every day");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
                return;
            }
        }
        scheduleDailyReminder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleDailyReminder();
            } else {
                // User denied — revert toggle
                switchReminders.setChecked(false);
                prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, false).apply();
                android.widget.Toast.makeText(this,
                        "Notification permission is needed for reminders.",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleDailyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, DailyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Fire at 9:00 AM daily
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }

        android.widget.Toast.makeText(this,
                "Daily reminders set for 9:00 AM", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void cancelDailyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, DailyReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    // ── About dialog ─────────────────────────────────────────────────────────

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("About LiteRise")
                .setMessage("LiteRise v1.0\n\n"
                        + "An adaptive literacy learning app designed for Grade 3 learners.\n\n"
                        + "Features:\n"
                        + "• Placement-based personalized learning path\n"
                        + "• 5 English skill modules\n"
                        + "• 10 game-based lesson types\n"
                        + "• XP, streaks & badge rewards\n\n"
                        + "© 2025 LiteRise. All rights reserved.")
                .setPositiveButton("OK", null)
                .show();
    }

    // ── Logout dialog ─────────────────────────────────────────────────────────

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    session.logout();
                    Intent intent = new Intent(SettingsActivity.this,
                            LoginRegisterSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}