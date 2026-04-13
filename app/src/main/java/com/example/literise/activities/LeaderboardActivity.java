package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.adapters.LeaderboardAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LeaderboardResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends BaseNavActivity {

    private RecyclerView rvLeaderboard;
    private ProgressBar progressLoading;
    private View layoutEmpty;
    private TextView tvFilterLabel;

    // Filter chips
    private TextView chipXp, chipStreak, chipPhonics, chipVocabulary,
            chipGrammar, chipComprehension, chipWriting;

    private String activeFilter = "xp";
    private SessionManager session;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        setupBottomNav(getNavIndex());

        session    = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);

        initViews();
        setupFilterChips();
        loadLeaderboard("xp");
    }

    private void initViews() {
        rvLeaderboard   = findViewById(R.id.rvLeaderboard);
        progressLoading = findViewById(R.id.progressLoading);
        layoutEmpty     = findViewById(R.id.layoutEmpty);
        tvFilterLabel   = findViewById(R.id.tvFilterLabel);

        chipXp            = findViewById(R.id.chipXp);
        chipStreak        = findViewById(R.id.chipStreak);
        chipPhonics       = findViewById(R.id.chipPhonics);
        chipVocabulary    = findViewById(R.id.chipVocabulary);
        chipGrammar       = findViewById(R.id.chipGrammar);
        chipComprehension = findViewById(R.id.chipComprehension);
        chipWriting       = findViewById(R.id.chipWriting);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFilterChips() {
        chipXp.setOnClickListener(v            -> applyFilter("xp",            chipXp));
        chipStreak.setOnClickListener(v        -> applyFilter("streak",        chipStreak));
        chipPhonics.setOnClickListener(v       -> applyFilter("phonics",       chipPhonics));
        chipVocabulary.setOnClickListener(v    -> applyFilter("vocabulary",    chipVocabulary));
        chipGrammar.setOnClickListener(v       -> applyFilter("grammar",       chipGrammar));
        chipComprehension.setOnClickListener(v -> applyFilter("comprehension", chipComprehension));
        chipWriting.setOnClickListener(v       -> applyFilter("writing",       chipWriting));
    }

    private void applyFilter(String filter, TextView selectedChip) {
        if (filter.equals(activeFilter)) return;
        activeFilter = filter;
        resetChips();
        selectedChip.setBackgroundResource(R.drawable.bg_filter_chip_active);
        selectedChip.setTextColor(0xFFFFFFFF);
        loadLeaderboard(filter);
    }

    private void resetChips() {
        TextView[] chips = {chipXp, chipStreak, chipPhonics, chipVocabulary,
                chipGrammar, chipComprehension, chipWriting};
        for (TextView chip : chips) {
            chip.setBackgroundResource(R.drawable.bg_filter_chip_inactive);
            chip.setTextColor(0xFF6B7280);
        }
    }

    private void loadLeaderboard(String filter) {
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        apiService.getLeaderboard(filter, 50).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                progressLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()
                        && response.body().getLeaderboard() != null
                        && !response.body().getLeaderboard().isEmpty()) {

                    tvFilterLabel.setText("Ranked by " + response.body().getFilterLabel());
                    int myId = session.getStudentId();
                    LeaderboardAdapter adapter = new LeaderboardAdapter(
                            response.body().getLeaderboard(), myId);
                    rvLeaderboard.setAdapter(adapter);
                    rvLeaderboard.setVisibility(View.VISIBLE);
                } else {
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                progressLoading.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected int getNavIndex() {
        return 4;
    }
}
