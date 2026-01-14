package com.example.literise.utils;

import com.example.literise.models.LearningModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to create and order learning modules based on placement test results
 * Creates 5 modules aligned with MATATAG English Curriculum Grade 3 (Official)
 * Orders modules by performance: weakest areas first (highest priority)
 *
 * MATATAG Grade 3 English Subdomains (Official):
 * 1. Phonics and Word Study (sounds to words)
 * 2. Vocabulary and Word Knowledge (words)
 * 3. Grammar Awareness and Grammatical Structures (sentences)
 * 4. Comprehending and Analyzing Text (discourse)
 * 5. Creating and Composing Text (discourse)
 *
 * Reference: FINAL MATATAG English CG 2023 Grades 2-10, Pages 61-76
 */
public class ModuleOrderingHelper {

    // Color gradients for 5 modules (inspired by design)
    private static final String[][] MODULE_GRADIENTS = {
        {"#A78BFA", "#DDD6FE"}, // Purple - Phonics and Word Study
        {"#FB7185", "#FECDD3"}, // Pink - Vocabulary and Word Knowledge
        {"#60A5FA", "#DBEAFE"}, // Blue - Grammar Awareness
        {"#34D399", "#D1FAE5"}, // Green - Comprehending and Analyzing Text
        {"#FBBF24", "#FEF3C7"}  // Yellow - Creating and Composing Text
    };

    /**
     * Create 5 MATATAG Grade 3 English modules with performance scores from placement test
     * Maps 4 placement test categories to 5 official curriculum subdomains using weighted formulas
     * @return List of modules ordered by priority (weakest first)
     */
    public static List<LearningModule> createModulesFromPlacementResults(
            double oralLanguageScore,
            double wordKnowledgeScore,
            double readingCompScore,
            double languageStructScore
    ) {
        List<LearningModule> modules = new ArrayList<>();

        // Module 1: Phonics and Word Study (EN3PWS)
        // Derived from word knowledge (primary) + reading comp (secondary)
        double phonicsWordStudyScore = (wordKnowledgeScore * 0.7 + readingCompScore * 0.3);
        modules.add(new LearningModule(
            1,
            "Phonics and Word Study",
            "Sight words and word patterns",
            "Phonics and Word Study",
            phonicsWordStudyScore,
            MODULE_GRADIENTS[0][0],
            MODULE_GRADIENTS[0][1]
        ));

        // Module 2: Vocabulary and Word Knowledge (EN3VWK)
        // Directly from word knowledge score
        modules.add(new LearningModule(
            2,
            "Vocabulary and Word Knowledge",
            "High-frequency words and meanings",
            "Vocabulary and Word Knowledge",
            wordKnowledgeScore,
            MODULE_GRADIENTS[1][0],
            MODULE_GRADIENTS[1][1]
        ));

        // Module 3: Grammar Awareness and Grammatical Structures (EN3GAGS)
        // Directly from language structure score
        modules.add(new LearningModule(
            3,
            "Grammar Awareness and Grammatical Structures",
            "Simple and compound sentences",
            "Grammar Awareness and Grammatical Structures",
            languageStructScore,
            MODULE_GRADIENTS[2][0],
            MODULE_GRADIENTS[2][1]
        ));

        // Module 4: Comprehending and Analyzing Text (EN3CAT)
        // Directly from reading comprehension score
        modules.add(new LearningModule(
            4,
            "Comprehending and Analyzing Text",
            "Understanding stories and informational texts",
            "Comprehending and Analyzing Text",
            readingCompScore,
            MODULE_GRADIENTS[3][0],
            MODULE_GRADIENTS[3][1]
        ));

        // Module 5: Creating and Composing Text (EN3CCT)
        // Derived from all categories (composite score)
        double creatingComposingScore = (
            oralLanguageScore * 0.15 +
            wordKnowledgeScore * 0.30 +
            readingCompScore * 0.30 +
            languageStructScore * 0.25
        );
        modules.add(new LearningModule(
            5,
            "Creating and Composing Text",
            "Writing stories and expressing ideas",
            "Creating and Composing Text",
            creatingComposingScore,
            MODULE_GRADIENTS[4][0],
            MODULE_GRADIENTS[4][1]
        ));

        // Order modules by priority (lowest score = highest priority)
        orderModulesByPriority(modules);

        return modules;
    }

    /**
     * Order modules by performance score (lowest first)
     * Sets priorityOrder field for each module
     */
    private static void orderModulesByPriority(List<LearningModule> modules) {
        // Sort by performance score (ascending - weakest first)
        Collections.sort(modules, new Comparator<LearningModule>() {
            @Override
            public int compare(LearningModule m1, LearningModule m2) {
                return Double.compare(m1.getPerformanceScore(), m2.getPerformanceScore());
            }
        });

        // Set priority order (1 = highest priority)
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setPriorityOrder(i + 1);
        }
    }

    /**
     * Apply locking logic based on placement level
     * Lower grade students have more locked modules
     * Total of 5 modules in Grade 3
     */
    public static void applyModuleLocking(List<LearningModule> modules, String placementLevel) {
        // Determine how many modules to unlock based on placement
        int unlockedCount;

        switch (placementLevel) {
            case "Grade 2":
            case "Low Grade 3":
                unlockedCount = 2; // Only top 2 priority modules unlocked
                break;
            case "Mid Grade 3":
                unlockedCount = 3; // Top 3 modules unlocked
                break;
            case "High Grade 3":
                unlockedCount = 4; // Top 4 modules unlocked
                break;
            case "Grade 4":
                unlockedCount = 5; // All 5 modules unlocked
                break;
            default:
                unlockedCount = 3; // Default: top 3 modules
        }

        // Lock modules beyond the unlock count
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setLocked(i >= unlockedCount);
        }
    }

    /**
     * Get module summary for dashboard header
     */
    public static String getModuleSummary(List<LearningModule> modules) {
        int totalModules = modules.size();
        long unlockedModules = modules.stream()
            .filter(m -> !m.isLocked())
            .count();

        return unlockedModules + " of " + totalModules + " modules unlocked";
    }

    /**
     * Get recommended module (first unlocked module with lowest score)
     */
    public static LearningModule getRecommendedModule(List<LearningModule> modules) {
        for (LearningModule module : modules) {
            if (!module.isLocked()) {
                return module;
            }
        }
        return modules.get(0); // Fallback to first module
    }
}
