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
 * Creates 8 modules aligned with MATATAG Reading and Literacy Curriculum (Key Stage 1)
 * Orders modules by performance: weakest areas first (highest priority)
 *
 * MATATAG KS1 Subdomains:
 * 1. Oral Language
 * 2. Phonological Awareness
 * 3. Phonics
 * 4. Word Study
 * 5. Grammar Awareness
 * 6. Vocabulary
 * 7. Comprehending and Analyzing Texts
 * 8. Creating and Composing Texts
 */
public class ModuleOrderingHelper {

    // Color gradients for modules (inspired by design)
    private static final String[][] MODULE_GRADIENTS = {
        {"#A78BFA", "#DDD6FE"}, // Purple
        {"#FB7185", "#FECDD3"}, // Pink
        {"#60A5FA", "#DBEAFE"}, // Blue
        {"#34D399", "#D1FAE5"}, // Green
        {"#FBBF24", "#FEF3C7"}, // Yellow
        {"#F472B6", "#FCE7F3"}, // Magenta
        {"#818CF8", "#E0E7FF"}, // Indigo
        {"#FB923C", "#FED7AA"}  // Orange
    };

    /**
     * Create 8 MATATAG Key Stage 1 modules with performance scores from placement test
     * Maps 4 placement test categories to 8 curriculum subdomains using weighted formulas
     * @return List of modules ordered by priority (weakest first)
     */
    public static List<LearningModule> createModulesFromPlacementResults(
            double oralLanguageScore,
            double wordKnowledgeScore,
            double readingCompScore,
            double languageStructScore
    ) {
        List<LearningModule> modules = new ArrayList<>();

        // Module 1: Oral Language (directly from placement test)
        modules.add(new LearningModule(
            1,
            "Oral Language",
            "Speaking and listening skills",
            "Oral Language",
            oralLanguageScore,
            MODULE_GRADIENTS[0][0],
            MODULE_GRADIENTS[0][1]
        ));

        // Module 2: Phonological Awareness (derived from word knowledge + oral language)
        double phonologicalScore = (wordKnowledgeScore * 0.6 + oralLanguageScore * 0.4);
        modules.add(new LearningModule(
            2,
            "Phonological Awareness",
            "Sound recognition and manipulation",
            "Phonological Awareness",
            phonologicalScore,
            MODULE_GRADIENTS[1][0],
            MODULE_GRADIENTS[1][1]
        ));

        // Module 3: Phonics (derived from word knowledge)
        double phonicsScore = (wordKnowledgeScore * 0.8 + readingCompScore * 0.2);
        modules.add(new LearningModule(
            3,
            "Phonics",
            "Letter-sound relationships",
            "Phonics",
            phonicsScore,
            MODULE_GRADIENTS[2][0],
            MODULE_GRADIENTS[2][1]
        ));

        // Module 4: Word Study (derived from word knowledge + reading comprehension)
        double wordStudyScore = (wordKnowledgeScore * 0.7 + readingCompScore * 0.3);
        modules.add(new LearningModule(
            4,
            "Word Study",
            "Understanding word patterns and meanings",
            "Word Study",
            wordStudyScore,
            MODULE_GRADIENTS[3][0],
            MODULE_GRADIENTS[3][1]
        ));

        // Module 5: Grammar Awareness (directly from language structure)
        modules.add(new LearningModule(
            5,
            "Grammar Awareness",
            "Sentence structure and rules",
            "Grammar Awareness",
            languageStructScore,
            MODULE_GRADIENTS[4][0],
            MODULE_GRADIENTS[4][1]
        ));

        // Module 6: Vocabulary (directly from word knowledge)
        modules.add(new LearningModule(
            6,
            "Vocabulary",
            "Word meanings and usage",
            "Vocabulary",
            wordKnowledgeScore,
            MODULE_GRADIENTS[5][0],
            MODULE_GRADIENTS[5][1]
        ));

        // Module 7: Comprehending and Analyzing Texts (directly from reading comp)
        modules.add(new LearningModule(
            7,
            "Comprehending and Analyzing Texts",
            "Understanding what you read",
            "Comprehending and Analyzing Texts",
            readingCompScore,
            MODULE_GRADIENTS[6][0],
            MODULE_GRADIENTS[6][1]
        ));

        // Module 8: Creating and Composing Texts (derived from all categories)
        double composingScore = (
            oralLanguageScore * 0.2 +
            wordKnowledgeScore * 0.3 +
            readingCompScore * 0.3 +
            languageStructScore * 0.2
        );
        modules.add(new LearningModule(
            8,
            "Creating and Composing Texts",
            "Writing stories and compositions",
            "Creating and Composing Texts",
            composingScore,
            MODULE_GRADIENTS[7][0],
            MODULE_GRADIENTS[7][1]
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
     */
    public static void applyModuleLocking(List<LearningModule> modules, String placementLevel) {
        // Determine how many modules to unlock based on placement
        int unlockedCount;

        switch (placementLevel) {
            case "Grade 2":
            case "Low Grade 3":
                unlockedCount = 3; // Only top 3 priority modules unlocked
                break;
            case "Mid Grade 3":
                unlockedCount = 5; // Top 5 modules unlocked
                break;
            case "High Grade 3":
            case "Grade 4":
                unlockedCount = 8; // All modules unlocked
                break;
            default:
                unlockedCount = 4; // Default: top 4 modules
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
