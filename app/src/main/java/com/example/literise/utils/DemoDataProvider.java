package com.example.literise.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Provides hardcoded demo data for offline mode
 */
public class DemoDataProvider {

    private static final String PREFS_NAME = "literise_demo_progress";
    private static final String KEY_TOTAL_XP = "total_xp";
    private static final String KEY_GAMES_COMPLETED = "games_completed"; // JSON map of lessonId -> Set<gameType>
    private static final String KEY_GAME_STATS = "game_stats"; // JSON map of lessonId_gameType -> stats

    // ==================== SENTENCE SCRAMBLE DATA ====================

    public static class Sentence {
        public int id;
        public String correctSentence;
        public List<String> scrambledWords;
        public float difficulty;
        public String category;
        public int gradeLevel;

        public Sentence(int id, String correctSentence, float difficulty, String category, int gradeLevel) {
            this.id = id;
            this.correctSentence = correctSentence;
            this.difficulty = difficulty;
            this.category = category;
            this.gradeLevel = gradeLevel;
            this.scrambledWords = generateScrambledWords(correctSentence);
        }

        private List<String> generateScrambledWords(String sentence) {
            String cleaned = sentence.replaceAll("[.!?,;:]", "");
            List<String> words = new ArrayList<>(Arrays.asList(cleaned.split("\\s+")));
            List<String> original = new ArrayList<>(words);

            // Shuffle until different from original
            int attempts = 0;
            do {
                Collections.shuffle(words);
                attempts++;
            } while (words.equals(original) && attempts < 10 && words.size() > 1);

            return words;
        }
    }

    public static List<Sentence> getSentences(int count) {
        List<Sentence> allSentences = new ArrayList<>();

        // Grade 4 sentences
        allSentences.add(new Sentence(1001, "The cat sat on the mat", 0.5f, "Simple", 4));
        allSentences.add(new Sentence(1002, "She goes to school every day", 0.6f, "Simple", 4));
        allSentences.add(new Sentence(1003, "The dog runs in the park", 0.5f, "Simple", 4));
        allSentences.add(new Sentence(1004, "My mother cooks delicious food", 0.6f, "Simple", 4));
        allSentences.add(new Sentence(1005, "The children play happily together", 0.7f, "Simple", 4));
        allSentences.add(new Sentence(1006, "I like to read books", 0.5f, "Simple", 4));
        allSentences.add(new Sentence(1007, "The bird sings in the morning", 0.6f, "Simple", 4));
        allSentences.add(new Sentence(1008, "We eat breakfast every day", 0.5f, "Simple", 4));

        // Grade 5 sentences
        allSentences.add(new Sentence(1009, "Maria finished her homework diligently", 0.8f, "Compound", 5));
        allSentences.add(new Sentence(1010, "The students are reading their books quietly", 0.9f, "Compound", 5));
        allSentences.add(new Sentence(1011, "The teacher explained the lesson clearly", 0.8f, "Compound", 5));
        allSentences.add(new Sentence(1012, "We visited the beautiful museum yesterday", 0.9f, "Compound", 5));
        allSentences.add(new Sentence(1013, "The quick brown fox jumps over the lazy dog", 1.0f, "Compound", 5));
        allSentences.add(new Sentence(1014, "My favorite subject in school is science", 0.8f, "Compound", 5));
        allSentences.add(new Sentence(1015, "The library has many interesting books to read", 0.9f, "Compound", 5));
        allSentences.add(new Sentence(1016, "She always helps her classmates with difficult problems", 1.0f, "Compound", 5));

        // Grade 6 sentences
        allSentences.add(new Sentence(1017, "Reading books regularly helps improve vocabulary skills", 1.2f, "Complex", 6));
        allSentences.add(new Sentence(1018, "My family and I visited the science museum yesterday", 1.3f, "Complex", 6));
        allSentences.add(new Sentence(1019, "The beautiful butterfly landed gently on the colorful flower", 1.2f, "Complex", 6));
        allSentences.add(new Sentence(1020, "Learning new words makes reading more enjoyable and interesting", 1.4f, "Complex", 6));
        allSentences.add(new Sentence(1021, "The hardworking students completed their challenging project successfully", 1.5f, "Complex", 6));
        allSentences.add(new Sentence(1022, "Environmental protection is important for our future generations", 1.4f, "Complex", 6));
        allSentences.add(new Sentence(1023, "Technology has greatly changed how we communicate with each other", 1.5f, "Complex", 6));
        allSentences.add(new Sentence(1024, "Critical thinking skills are essential for solving complex problems", 1.6f, "Complex", 6));

        // Shuffle and return requested count
        Collections.shuffle(allSentences);
        return allSentences.subList(0, Math.min(count, allSentences.size()));
    }

    // ==================== WORD HUNT DATA ====================

    public static class WordItem {
        public int id;
        public String word;
        public String definition;
        public String hint;
        public int gradeLevel;

        public WordItem(int id, String word, String definition, String hint, int gradeLevel) {
            this.id = id;
            this.word = word;
            this.definition = definition;
            this.hint = hint;
            this.gradeLevel = gradeLevel;
        }
    }

    public static List<WordItem> getWords(int count) {
        List<WordItem> allWords = new ArrayList<>();

        // Grade 4-5 words
        allWords.add(new WordItem(1, "HAPPY", "Feeling joy or pleasure", "Opposite of sad", 4));
        allWords.add(new WordItem(2, "BRAVE", "Ready to face danger", "Not afraid", 4));
        allWords.add(new WordItem(3, "KIND", "Friendly and generous", "Nice to others", 4));
        allWords.add(new WordItem(4, "SMART", "Having quick intelligence", "Very clever", 4));
        allWords.add(new WordItem(5, "QUICK", "Moving fast", "Not slow", 4));
        allWords.add(new WordItem(6, "PEACE", "Freedom from war", "Calm and quiet", 5));
        allWords.add(new WordItem(7, "DREAM", "Images during sleep", "What you see when sleeping", 5));
        allWords.add(new WordItem(8, "LIGHT", "Brightness from the sun", "Opposite of dark", 5));
        allWords.add(new WordItem(9, "WATER", "Clear liquid we drink", "H2O", 4));
        allWords.add(new WordItem(10, "EARTH", "Our planet", "The world we live on", 5));
        allWords.add(new WordItem(11, "MUSIC", "Sounds arranged pleasingly", "Art of sound", 5));
        allWords.add(new WordItem(12, "STORY", "A narrative of events", "A tale", 4));
        allWords.add(new WordItem(13, "FRIEND", "A person you like", "Someone close to you", 4));
        allWords.add(new WordItem(14, "LEARN", "To gain knowledge", "To study", 5));
        allWords.add(new WordItem(15, "THINK", "To use your mind", "To reason", 5));

        // Grade 6 words
        allWords.add(new WordItem(16, "CURIOUS", "Eager to know", "Wanting to learn", 6));
        allWords.add(new WordItem(17, "EXPLORE", "To search or investigate", "To discover", 6));
        allWords.add(new WordItem(18, "CREATE", "To make something new", "To invent", 6));
        allWords.add(new WordItem(19, "BELIEVE", "To accept as true", "To have faith", 6));
        allWords.add(new WordItem(20, "IMAGINE", "To form mental images", "To picture in mind", 6));

        // Shuffle and return requested count
        Collections.shuffle(allWords);
        return allWords.subList(0, Math.min(count, allWords.size()));
    }

    // ==================== PROGRESS TRACKING ====================

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getTotalXP(Context context) {
        return getPrefs(context).getInt(KEY_TOTAL_XP, 0);
    }

    public static void addXP(Context context, int xp) {
        int current = getTotalXP(context);
        getPrefs(context).edit().putInt(KEY_TOTAL_XP, current + xp).apply();
    }

    public static void saveGameCompleted(Context context, int lessonId, String gameType, int xp, float accuracy, int timeSeconds) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = new Gson();

        // Update games completed set
        String gamesJson = prefs.getString(KEY_GAMES_COMPLETED, "{}");
        Type mapType = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> gamesMap = gson.fromJson(gamesJson, mapType);
        if (gamesMap == null) gamesMap = new HashMap<>();

        String lessonKey = String.valueOf(lessonId);
        if (!gamesMap.containsKey(lessonKey)) {
            gamesMap.put(lessonKey, new HashSet<>());
        }
        gamesMap.get(lessonKey).add(gameType);

        // Save game stats
        String statsJson = prefs.getString(KEY_GAME_STATS, "{}");
        Type statsType = new TypeToken<Map<String, GameStats>>(){}.getType();
        Map<String, GameStats> statsMap = gson.fromJson(statsJson, statsType);
        if (statsMap == null) statsMap = new HashMap<>();

        String statsKey = lessonId + "_" + gameType;
        statsMap.put(statsKey, new GameStats(xp, accuracy, timeSeconds));

        // Add XP
        addXP(context, xp);

        // Save all
        prefs.edit()
                .putString(KEY_GAMES_COMPLETED, gson.toJson(gamesMap))
                .putString(KEY_GAME_STATS, gson.toJson(statsMap))
                .apply();
    }

    public static Set<String> getCompletedGames(Context context, int lessonId) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = new Gson();

        String gamesJson = prefs.getString(KEY_GAMES_COMPLETED, "{}");
        Type mapType = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> gamesMap = gson.fromJson(gamesJson, mapType);

        if (gamesMap == null || !gamesMap.containsKey(String.valueOf(lessonId))) {
            return new HashSet<>();
        }
        return gamesMap.get(String.valueOf(lessonId));
    }

    public static int getGamesPlayedCount(Context context, int lessonId) {
        return getCompletedGames(context, lessonId).size();
    }

    public static GameStats getGameStats(Context context, int lessonId, String gameType) {
        SharedPreferences prefs = getPrefs(context);
        Gson gson = new Gson();

        String statsJson = prefs.getString(KEY_GAME_STATS, "{}");
        Type statsType = new TypeToken<Map<String, GameStats>>(){}.getType();
        Map<String, GameStats> statsMap = gson.fromJson(statsJson, statsType);

        if (statsMap == null) return null;
        return statsMap.get(lessonId + "_" + gameType);
    }

    public static void resetProgress(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    public static class GameStats {
        public int xpEarned;
        public float accuracy;
        public int timeSeconds;

        public GameStats(int xpEarned, float accuracy, int timeSeconds) {
            this.xpEarned = xpEarned;
            this.accuracy = accuracy;
            this.timeSeconds = timeSeconds;
        }

        public String getFormattedTime() {
            int minutes = timeSeconds / 60;
            int seconds = timeSeconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
