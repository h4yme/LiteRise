package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.adapters.BadgeGridAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.BadgeItem;
import com.example.literise.models.BadgesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BadgesActivity extends BaseNavActivity {

    private TextView tvBadgeCount, tvBadgesEarned, tvBadgesTotal;
    private ProgressBar progressBadges;
    private RecyclerView rvBadges;
    private LinearLayout layoutLoading, layoutEmpty;

    private SessionManager session;
    private BadgeGridAdapter adapter;
    private List<BadgeItem> badgeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        session = new SessionManager(this);

        tvBadgeCount    = findViewById(R.id.tvBadgeCount);
        tvBadgesEarned  = findViewById(R.id.tvBadgesEarned);
        tvBadgesTotal   = findViewById(R.id.tvBadgesTotal);
        progressBadges  = findViewById(R.id.progressBadges);
        rvBadges        = findViewById(R.id.rvBadges);
        layoutLoading   = findViewById(R.id.layoutLoading);
        layoutEmpty     = findViewById(R.id.layoutEmpty);

        rvBadges.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new BadgeGridAdapter(this, badgeList);
        rvBadges.setAdapter(adapter);

        setupBottomNav(getNavIndex());
        fetchBadges();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBadges();
    }

    private void fetchBadges() {
        int studentId = session.getStudentId();
        if (studentId <= 0) {
            showEmpty();
            return;
        }

        showLoading(true);

        ApiClient.getClient(this).create(ApiService.class)
                .getBadges(studentId)
                .enqueue(new Callback<BadgesResponse>() {
                    @Override
                    public void onResponse(Call<BadgesResponse> call,
                                           Response<BadgesResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {
                            bindBadges(response.body());
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<BadgesResponse> call, Throwable t) {
                        showLoading(false);
                        showEmpty();
                    }
                });
    }

    private void bindBadges(BadgesResponse data) {
        int earned = data.getEarnedCount();
        int total  = data.getTotalCount();

        tvBadgeCount.setText(earned + " / " + total);
        if (tvBadgesEarned != null) tvBadgesEarned.setText(String.valueOf(earned));
        if (tvBadgesTotal  != null) tvBadgesTotal.setText(String.valueOf(total));
        if (progressBadges != null) {
            progressBadges.setMax(total > 0 ? total : 15);
            progressBadges.setProgress(earned);
        }

        badgeList.clear();
        if (data.getBadges() != null) badgeList.addAll(data.getBadges());
        adapter.notifyDataSetChanged();

        boolean hasBadges = !badgeList.isEmpty();
        rvBadges.setVisibility(hasBadges ? View.VISIBLE : View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(hasBadges ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean loading) {
        if (layoutLoading != null)
            layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) rvBadges.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvBadges.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getNavIndex() { return 3; }
}