package com.example.literise.utils;

import com.example.literise.models.ScrambledSentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameDataManager {

    private static final String[] EASY_SENTENCES = {
            "Maria finished her homework diligently",
            "The cat sleeps on the mat",
            "I love reading books every day",
            "She walks to school happily",
            "The sun shines brightly today"
    };

    private static final String[] MEDIUM_SENTENCES = {
            "The students completed their assignments before the deadline",
            "Reading comprehension improves with regular practice",
            "She organized her notes carefully for the exam",
            "The teacher explained the grammar rules clearly",
            "They collaborated effectively on the group project"
    };

    private static final String[] HARD_SENTENCES = {
            "Developing strong literacy skills requires consistent dedication and focused practice",
            "The researchers meticulously analyzed the comprehensive data before publishing their findings",
            "Educators continuously implement innovative strategies to enhance student engagement",
            "Critical thinking abilities significantly improve through analytical reading exercises",
            "Pronunciation accuracy develops gradually with persistent oral practice sessions"
    };

    public static List<ScrambledSentence> getScrambledSentences(int count, String difficulty) {
        List<ScrambledSentence> sentences = new ArrayList<>();
        String[] sourceArray;

        switch (difficulty.toLowerCase()) {
            case "easy":
                sourceArray = EASY_SENTENCES;
                break;
            case "medium":
                sourceArray = MEDIUM_SENTENCES;
                break;
            case "hard":
                sourceArray = HARD_SENTENCES;
                break;
            default:
                sourceArray = MEDIUM_SENTENCES;
        }

        Random random = new Random();
        for (int i = 0; i < Math.min(count, sourceArray.length); i++) {
            int index = random.nextInt(sourceArray.length);
            sentences.add(new ScrambledSentence(i, sourceArray[index], difficulty));
        }

        return sentences;
    }

    public static List<ScrambledSentence> getMixedScrambledSentences(int count) {
        List<ScrambledSentence> sentences = new ArrayList<>();
        String[] difficulties = {"easy", "medium", "hard"};
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            String difficulty = difficulties[random.nextInt(difficulties.length)];
            String[] sourceArray;

            switch (difficulty) {
                case "easy":
                    sourceArray = EASY_SENTENCES;
                    break;
                case "medium":
                    sourceArray = MEDIUM_SENTENCES;
                    break;
                case "hard":
                    sourceArray = HARD_SENTENCES;
                    break;
                default:
                    sourceArray = MEDIUM_SENTENCES;
            }

            int index = random.nextInt(sourceArray.length);
            sentences.add(new ScrambledSentence(i, sourceArray[index], difficulty));
        }

        return sentences;
    }
}
