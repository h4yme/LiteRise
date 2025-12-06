package com.example.literise.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ModulePriorityManager - Manages module ordering based on assessment results
 *
 * Tracks student performance by module type and orders modules from
 * weakest (highest priority) to strongest (lowest priority) for personalized learning
 */
public class ModulePriorityManager {
    private static final String PREF_NAME = "ModulePriorities";
    private static final String KEY_MODULE_PERFORMANCE = "module_performance";
    private static final String KEY_ORDERED_MODULES = "ordered_modules";

    private SharedPreferences prefs;
    private Gson gson;

    // Map item types to module categories
    private static final Map<String, String> ITEM_TYPE_TO_MODULE = new HashMap<>();
    static {
        ITEM_TYPE_TO_MODULE.put("Reading", "Reading Comprehension");
        ITEM_TYPE_TO_MODULE.put("Comprehension", "Reading Comprehension");
        ITEM_TYPE_TO_MODULE.put("Pronunciation", "Phonics & Pronunciation");
        ITEM_TYPE_TO_MODULE.put("Phonics", "Phonics & Pronunciation");
        ITEM_TYPE_TO_MODULE.put("Vocabulary", "Vocabulary Building");
        ITEM_TYPE_TO_MODULE.put("Word", "Vocabulary Building");
        ITEM_TYPE_TO_MODULE.put("Grammar", "Grammar & Syntax");
        ITEM_TYPE_TO_MODULE.put("Syntax", "Grammar & Syntax");
        ITEM_TYPE_TO_MODULE.put("Fluency", "Reading Fluency");
        ITEM_TYPE_TO_MODULE.put("Spelling", "Spelling & Writing");
        ITEM_TYPE_TO_MODULE.put("Writing", "Spelling & Writing");
    }

    // All 6 module categories in default pedagogical order
    public static final String[] ALL_MODULES = {
            "Phonics & Pronunciation",      // Foundation
            "Vocabulary Building",           // Building blocks
            "Reading Comprehension",         // Core skill
            "Grammar & Syntax",              // Structure
            "Reading Fluency",               // Application
            "Spelling & Writing"             // Advanced
    };

    public ModulePriorityManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Track performance for a question during assessment
     *
     * @param itemType The type of question (from Question.getItemType())
     * @param isCorrect Whether the student answered correctly
     */
    public void recordPerformance(String itemType, boolean isCorrect) {
        String module = mapItemTypeToModule(itemType);
        if (module == null) return;

        Map<String, ModulePerformance> performance = getPerformanceMap();

        ModulePerformance modulePerf = performance.get(module);
        if (modulePerf == null) {
            modulePerf = new ModulePerformance(module);
            performance.put(module, modulePerf);
        }

        modulePerf.addAttempt(isCorrect);
        savePerformanceMap(performance);
    }

    /**
     * Calculate and save ordered modules based on performance (weakest to strongest)
     */
    public void calculateModulePriorities() {
        Map<String, ModulePerformance> performance = getPerformanceMap();

        List<ModulePerformance> moduleList = new ArrayList<>();

        // Add modules with performance data
        for (String module : ALL_MODULES) {
            ModulePerformance perf = performance.get(module);
            if (perf != null && perf.getTotalAttempts() > 0) {
                moduleList.add(perf);
            }
        }

        // Sort by performance (lowest accuracy first = highest priority)
        Collections.sort(moduleList, (a, b) ->
            Double.compare(a.getAccuracy(), b.getAccuracy())
        );

        // Get ordered module names
        List<String> orderedModules = new ArrayList<>();
        for (ModulePerformance perf : moduleList) {
            orderedModules.add(perf.getModuleName());
        }

        // Add modules not tested yet (in default order)
        for (String module : ALL_MODULES) {
            if (!orderedModules.contains(module)) {
                orderedModules.add(module);
            }
        }

        saveOrderedModules(orderedModules);
    }

    /**
     * Get ordered modules (weakest to strongest)
     * Returns default order if no assessment data available
     */
    public List<String> getOrderedModules() {
        String json = prefs.getString(KEY_ORDERED_MODULES, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> ordered = gson.fromJson(json, type);
            if (ordered != null && !ordered.isEmpty()) {
                return ordered;
            }
        }

        // Return default pedagogical order
        List<String> defaultOrder = new ArrayList<>();
        Collections.addAll(defaultOrder, ALL_MODULES);
        return defaultOrder;
    }

    /**
     * Get performance summary for a specific module
     */
    public ModulePerformance getModulePerformance(String moduleName) {
        Map<String, ModulePerformance> performance = getPerformanceMap();
        return performance.get(moduleName);
    }

    /**
     * Clear all performance data (for new assessment)
     */
    public void clearPerformance() {
        prefs.edit()
                .remove(KEY_MODULE_PERFORMANCE)
                .remove(KEY_ORDERED_MODULES)
                .apply();
    }

    // Helper methods

    private String mapItemTypeToModule(String itemType) {
        if (itemType == null) return null;

        for (Map.Entry<String, String> entry : ITEM_TYPE_TO_MODULE.entrySet()) {
            if (itemType.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, ModulePerformance> getPerformanceMap() {
        String json = prefs.getString(KEY_MODULE_PERFORMANCE, null);
        if (json != null) {
            Type type = new TypeToken<Map<String, ModulePerformance>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new HashMap<>();
    }

    private void savePerformanceMap(Map<String, ModulePerformance> performance) {
        String json = gson.toJson(performance);
        prefs.edit().putString(KEY_MODULE_PERFORMANCE, json).apply();
    }

    private void saveOrderedModules(List<String> modules) {
        String json = gson.toJson(modules);
        prefs.edit().putString(KEY_ORDERED_MODULES, json).apply();
    }

    /**
     * Inner class to track performance for a module
     */
    public static class ModulePerformance {
        private String moduleName;
        private int correctAnswers;
        private int totalAttempts;

        public ModulePerformance(String moduleName) {
            this.moduleName = moduleName;
            this.correctAnswers = 0;
            this.totalAttempts = 0;
        }

        public void addAttempt(boolean isCorrect) {
            totalAttempts++;
            if (isCorrect) {
                correctAnswers++;
            }
        }

        public double getAccuracy() {
            if (totalAttempts == 0) return 0.5; // Default 50% for untested
            return (double) correctAnswers / totalAttempts;
        }

        public String getModuleName() {
            return moduleName;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public int getTotalAttempts() {
            return totalAttempts;
        }

        public String getPerformanceLevel() {
            double accuracy = getAccuracy();
            if (accuracy < 0.4) return "Needs Practice";
            if (accuracy < 0.6) return "Developing";
            if (accuracy < 0.8) return "Good";
            return "Excellent";
        }
    }
}
