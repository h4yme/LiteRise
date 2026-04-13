package com.example.literise.activities;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;
import com.example.literise.adapters.LeaderboardAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LeaderboardEntry;
import com.example.literise.models.LeaderboardResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends BaseNavActivity {

    private static final int[] AVATAR_COLORS = {
            0xFF7C3AED, 0xFF2563EB, 0xFF059669, 0xFFDC2626,
            0xFFD97706, 0xFF7C3AED, 0xFF0891B2, 0xFF9333EA,
    };

    private RecyclerView rvLeaderboard;
    private ProgressBar progressLoading;
    private View layoutEmpty;
    private TextView tvFilterLabel;
    private LinearLayout podiumSection;

    // Podium views
    private FrameLayout avatar1st, avatar2nd, avatar3rd;
    private TextView initial1st, initial2nd, initial3rd;
    private TextView podName1st, podName2nd, podName3rd;
    private TextView podScore1st, podScore2nd, podScore3rd;
    private LottieAnimationView lottieCrown;

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
        podiumSection   = findViewById(R.id.podiumSection);

        avatar1st  = findViewById(R.id.avatar1st);
        avatar2nd  = findViewById(R.id.avatar2nd);
        avatar3rd  = findViewById(R.id.avatar3rd);
        initial1st = findViewById(R.id.avatarInitial1st);
        initial2nd = findViewById(R.id.avatarInitial2nd);
        initial3rd = findViewById(R.id.avatarInitial3rd);
        podName1st  = findViewById(R.id.podName1st);
        podName2nd  = findViewById(R.id.podName2nd);
        podName3rd  = findViewById(R.id.podName3rd);
        podScore1st = findViewById(R.id.podScore1st);
        podScore2nd = findViewById(R.id.podScore2nd);
        podScore3rd = findViewById(R.id.podScore3rd);
        lottieCrown = findViewById(R.id.lottieCrown);

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

    private void applyFilter(String filter, TextView selected) {
        if (filter.equals(activeFilter)) return;
        activeFilter = filter;
        resetChips();
        selected.setBackgroundResource(R.drawable.bg_filter_chip_active);
        selected.setTextColor(0xFFFFFFFF);
        loadLeaderboard(filter);
    }

    private void resetChips() {
        for (TextView chip : new TextView[]{chipXp, chipStreak, chipPhonics,
                chipVocabulary, chipGrammar, chipComprehension, chipWriting}) {
            chip.setBackgroundResource(R.drawable.bg_filter_chip_inactive);
            chip.setTextColor(0xFF6B7280);
        }
    }

    private void loadLeaderboard(String filter) {
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        podiumSection.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        apiService.getLeaderboard(filter, 50).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                progressLoading.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null
                        || !response.body().isSuccess()
                        || response.body().getLeaderboard() == null
                        || response.body().getLeaderboard().isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                List<LeaderboardEntry> all = response.body().getLeaderboard();
                tvFilterLabel.setText("Ranked by " + response.body().getFilterLabel());

                // Populate podium (top 3)
                populatePodium(all);

                // RecyclerView shows rank 4+
                List<LeaderboardEntry> rest = all.size() > 3 ? all.subList(3, all.size()) : List.of();
                int myId = session.getStudentId();
                rvLeaderboard.setAdapter(new LeaderboardAdapter(rest, myId));
                rvLeaderboard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                progressLoading.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void populatePodium(List<LeaderboardEntry> all) {
        if (all.size() < 1) return;
        podiumSection.setVisibility(View.VISIBLE);

        // 1st place
        LeaderboardEntry e1 = all.get(0);
        setAvatarCircle(avatar1st, initial1st, e1.getName(), 0xFFFFD700);
        podName1st.setText(firstName(e1.getName()));
        podScore1st.setText(String.valueOf(e1.getValue()));
        lottieCrown.playAnimation();

        // 2nd place
        if (all.size() >= 2) {
            LeaderboardEntry e2 = all.get(1);
            setAvatarCircle(avatar2nd, initial2nd, e2.getName(), 0xFFC8D0DA);
            podName2nd.setText(firstName(e2.getName()));
            podScore2nd.setText(String.valueOf(e2.getValue()));
        } else {
            findViewById(R.id.pod2nd).setVisibility(View.INVISIBLE);
        }

        // 3rd place
        if (all.size() >= 3) {
            LeaderboardEntry e3 = all.get(2);
            setAvatarCircle(avatar3rd, initial3rd, e3.getName(), 0xFFE8A87C);
            podName3rd.setText(firstName(e3.getName()));
            podScore3rd.setText(String.valueOf(e3.getValue()));
        } else {
            findViewById(R.id.pod3rd).setVisibility(View.INVISIBLE);
        }
    }

    private void setAvatarCircle(FrameLayout frame, TextView initial, String name, int ringColor) {
        String letter = (name != null && !name.isEmpty())
                ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        initial.setText(letter);
        int bg = avatarColor(name);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(bg);
        gd.setStroke(3, ringColor);
        frame.setBackground(gd);
    }

    /** Return just the first name for the podium label */
    private String firstName(String fullName) {
        if (fullName == null) return "";
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(0, space) : fullName;
    }

    private int avatarColor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(name.charAt(0)) % AVATAR_COLORS.length];
    }

    @Override
    protected int getNavIndex() { return 4; }
}
