package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.adapters.ModuleAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.CheckModulesCompleteResponse;
import com.example.literise.models.LearningModule;
import com.example.literise.utils.ModulePriorityManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModulesViewActivity extends BaseNavActivity {

    private TextView tvOverallPercent, tvOverallLessons;
    private ProgressBar progressOverall;
    private RecyclerView rvModulesList;

    private SessionManager session;
    private ModulePriorityManager priorityManager;
    private List<LearningModule> modules;
    private ModuleAdapter moduleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules_view);

        session = new SessionManager(this);
        priorityManager = new ModulePriorityManager(this);

        tvOverallPercent  = findViewById(R.id.tvOverallPercent);
        tvOverallLessons  = findViewById(R.id.tvOverallLessons);
        progressOverall   = findViewById(R.id.progressOverall);
        rvModulesList     = findViewById(R.id.rvModulesList);

        rvModulesList.setLayoutManager(new LinearLayoutManager(this));

        setupBottomNav(getNavIndex());
        loadModules();
    }

    private void loadModules() {
        priorityManager.calculateModulePrioritiesFromPlacementTest();
        List<String> orderedNames = priorityManager.getOrderedModules();

        modules = new ArrayList<>();
        String[] gradients = {"#FF6B6B","#FF8E53","#4ECDC4","#44A08D","#A770EF",
                              "#CF57A3","#FFD93D","#FFA93D","#667EEA","#764BA2"};

        for (int i = 0; i < orderedNames.size(); i++) {
            String name = orderedNames.get(i);
            int score   = getScore(name);
            LearningModule m = new LearningModule(
                    getModuleId(name), name, getSubtitle(name), getDomain(name),
                    score / 100.0, gradients[i * 2], gradients[i * 2 + 1]);
            m.setPriorityOrder(i + 1);
            m.setLocked(i != 0);
            m.setTotalLessons(getLessonCount(name));
            modules.add(m);
        }

        // Calculate overall progress
        int totalLessons = 0, completedLessons = 0;
        for (LearningModule m : modules) {
            totalLessons += m.getTotalLessons();
            completedLessons += m.getCompletedLessons();
        }
        int pct = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;
        tvOverallPercent.setText(pct + "%");
        tvOverallLessons.setText(completedLessons + " / " + totalLessons + " lessons");
        progressOverall.setProgress(pct);

        moduleAdapter = new ModuleAdapter(this, modules, module -> {
            if (module.isLocked()) {
                android.widget.Toast.makeText(this,
                        "Complete previous modules to unlock " + module.getTitle(),
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ModuleLadderActivity.class);
            intent.putExtra("module_id", module.getModuleId());
            intent.putExtra("module_name", module.getTitle());
            intent.putExtra("module_domain", module.getDomain());
            intent.putExtra("module_level", module.getLevel());
            intent.putExtra("priority", module.getPriorityOrder());
            startActivity(intent);
        });
        rvModulesList.setAdapter(moduleAdapter);

        // Fetch real completion counts from API
        fetchCompletionData();
    }

    private void fetchCompletionData() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiClient.getClient(this).create(ApiService.class)
                .checkModulesComplete(studentId)
                .enqueue(new Callback<CheckModulesCompleteResponse>() {
                    @Override
                    public void onResponse(Call<CheckModulesCompleteResponse> call,
                                           Response<CheckModulesCompleteResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || !response.body().isSuccess()) return;

                        CheckModulesCompleteResponse body = response.body();
                        if (body.getTotalCount() <= 0 || modules == null || modules.isEmpty()) return;

                        int nodesPerModule = body.getTotalCount() / modules.size();
                        if (nodesPerModule <= 0) return;

                        int remaining = body.getCompletedCount();
                        int completedModules = body.getCompletedCount() / nodesPerModule;

                        for (int i = 0; i < modules.size(); i++) {
                            modules.get(i).setLocked(i > completedModules);
                            int modCompleted = Math.min(remaining, nodesPerModule);
                            modules.get(i).setCompletedLessons(Math.max(0, modCompleted));
                            modules.get(i).setTotalLessons(nodesPerModule);
                            remaining -= modCompleted;
                        }
                        moduleAdapter.notifyDataSetChanged();

                        // Refresh overall progress bar
                        int total = 0, completed = 0;
                        for (LearningModule m : modules) {
                            total += m.getTotalLessons();
                            completed += m.getCompletedLessons();
                        }
                        int pct = total > 0 ? (completed * 100 / total) : 0;
                        tvOverallPercent.setText(pct + "%");
                        tvOverallLessons.setText(completed + " / " + total + " lessons");
                        progressOverall.setProgress(pct);
                    }

                    @Override
                    public void onFailure(Call<CheckModulesCompleteResponse> call, Throwable t) {
                        // Silently fail — static data already shown
                    }
                });
    }

    private int getScore(String name) {
        switch (name) {
            case "Phonics and Word Study":                       return session.getCategoryScore("Cat1_PhonicsWordStudy");
            case "Vocabulary and Word Knowledge":                return session.getCategoryScore("Cat2_VocabularyWordKnowledge");
            case "Grammar Awareness and Grammatical Structures": return session.getCategoryScore("Cat3_GrammarAwareness");
            case "Comprehending and Analyzing Text":             return session.getCategoryScore("Cat4_ComprehendingText");
            case "Creating and Composing Text":                  return session.getCategoryScore("Cat5_CreatingComposing");
            default: return 50;
        }
    }

    private int getModuleId(String name) {
        switch (name) {
            case "Phonics and Word Study":                       return 1;
            case "Vocabulary and Word Knowledge":                return 2;
            case "Grammar Awareness and Grammatical Structures": return 3;
            case "Comprehending and Analyzing Text":             return 4;
            case "Creating and Composing Text":                  return 5;
            default: return 1;
        }
    }

    private String getDomain(String name) {
        if (name.contains("Phonics"))      return "Phonics";
        if (name.contains("Vocabulary"))   return "Vocabulary";
        if (name.contains("Grammar"))      return "Grammar";
        if (name.contains("Comprehend"))   return "Comprehending";
        return "Writing";
    }

    private String getSubtitle(String name) {
        if (name.contains("Phonics"))      return "Letter sounds and word patterns";
        if (name.contains("Vocabulary"))   return "Building your word bank";
        if (name.contains("Grammar"))      return "Sentence structure and rules";
        if (name.contains("Comprehend"))   return "Understanding what you read";
        return "Express your ideas in writing";
    }

    private int getLessonCount(String name) {
        if (name.contains("Phonics"))      return 12;
        if (name.contains("Vocabulary"))   return 10;
        if (name.contains("Grammar"))      return 8;
        if (name.contains("Comprehend"))   return 10;
        return 8;
    }

    @Override
    protected int getNavIndex() { return 1; }
}
