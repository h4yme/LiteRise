package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.BadgesResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewActivity extends BaseNavActivity {

    private TextView tvProfileName, tvProfileLevel, tvProfileNameDetail,
                     tvProfileEmail, tvProfilePlacementLevel,
                     tvProfileXP, tvProfileStreak, tvProfileBadges;

    private MaterialCardView cardSettings, cardLogOut;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        session = new SessionManager(this);

        tvProfileName           = findViewById(R.id.tvProfileName);
        tvProfileLevel          = findViewById(R.id.tvProfileLevel);
        tvProfileNameDetail     = findViewById(R.id.tvProfileNameDetail);
        tvProfileEmail          = findViewById(R.id.tvProfileEmail);
        tvProfilePlacementLevel = findViewById(R.id.tvProfilePlacementLevel);
        tvProfileXP             = findViewById(R.id.tvProfileXP);
        tvProfileStreak         = findViewById(R.id.tvProfileStreak);
        tvProfileBadges         = findViewById(R.id.tvProfileBadges);
        cardSettings            = findViewById(R.id.cardSettings);
        cardLogOut              = findViewById(R.id.cardLogOut);

        android.widget.ImageView ivSettings = findViewById(R.id.ivSettingsBtn);
        if (ivSettings != null) ivSettings.setOnClickListener(v -> openSettings());

        cardSettings.setOnClickListener(v -> openSettings());
        cardLogOut.setOnClickListener(v -> showLogoutDialog());

        setupBottomNav(getNavIndex());
        loadProfile();
    }

    private void loadProfile() {
        String nickname  = session.getNickname();
        String fullname  = session.getFullname();
        String email     = session.getEmail();
        String level     = session.getPlacementLevel();
        int xp           = session.getXP();
        int streak       = session.getStreak();

        String displayName = (nickname != null && !nickname.isEmpty()) ? nickname
                           : (fullname != null && !fullname.isEmpty()) ? fullname
                           : "Student";

        tvProfileName.setText(displayName);
        tvProfileLevel.setText("LiteRise Learner");
        tvProfileNameDetail.setText(fullname != null && !fullname.isEmpty() ? fullname : displayName);
        tvProfileEmail.setText(email != null && !email.isEmpty() ? email : "—");
        tvProfilePlacementLevel.setText(level != null && !level.isEmpty() ? level : "Not tested yet");
        tvProfileXP.setText(String.valueOf(xp));
        tvProfileStreak.setText(String.valueOf(streak));
        tvProfileBadges.setText("—"); // will be updated from API
        fetchBadgeCount();
    }

    private void fetchBadgeCount() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;
        ApiClient.getClient(this).create(ApiService.class)
                .getBadges(studentId)
                .enqueue(new Callback<BadgesResponse>() {
                    @Override
                    public void onResponse(Call<BadgesResponse> call,
                                           Response<BadgesResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            tvProfileBadges.setText(
                                    String.valueOf(response.body().getEarnedCount()));
                        }
                    }
                    @Override
                    public void onFailure(Call<BadgesResponse> call, Throwable t) { }
                });
    }

    private void openSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    session.logout();
                    Intent intent = new Intent(this, LoginRegisterSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected int getNavIndex() { return 4; }
}
