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
// ==================== FILL IN THE BLANKS DATA ====================

    public static class FillQuestion {
        public String beforeBlank;   // Text before the blank
        public String afterBlank;    // Text after the blank (may be empty)
        public String correctAnswer; // The correct word
        public String[] options;     // All 4 options (shuffled in activity)
        public String category;

        public FillQuestion(String before, String after, String answer, String[] wrongOptions, String cat) {
            this.beforeBlank = before;
            this.afterBlank = after;
            this.correctAnswer = answer;
            this.category = cat;
            // Build options array: correct + wrong, shuffle will happen in activity
            this.options = new String[wrongOptions.length + 1];
            this.options[0] = answer;
            System.arraycopy(wrongOptions, 0, this.options, 1, wrongOptions.length);
        }
    }

    public static List<FillQuestion> getFillQuestions(String domain, int count) {
        List<FillQuestion> questions = new ArrayList<>();
        String d = (domain == null) ? "" : domain.toLowerCase();

        if (d.contains("phonics")) {
            questions.add(new FillQuestion("The cat ", " on the mat.", "sat", new String[]{"bit", "sit", "put"}, "phonics"));
            questions.add(new FillQuestion("She can ", " her name clearly.", "spell", new String[]{"tell", "yell", "sell"}, "phonics"));
            questions.add(new FillQuestion("The ", " shines bright in the sky.", "sun", new String[]{"son", "fun", "bun"}, "phonics"));
            questions.add(new FillQuestion("The word 'bike' has a ", " vowel sound.", "long", new String[]{"short", "silent", "mixed"}, "phonics"));
            questions.add(new FillQuestion("She ", " a song for her class.", "sang", new String[]{"singed", "sings", "singing"}, "phonics"));
            questions.add(new FillQuestion("The bird ", " past the window.", "flew", new String[]{"fly", "flow", "flown"}, "phonics"));
            questions.add(new FillQuestion("He ", " the ball very hard.", "hit", new String[]{"hot", "hat", "hut"}, "phonics"));
            questions.add(new FillQuestion("Words like 'cat', 'bat', 'hat' all ", ".", "rhyme", new String[]{"blend", "match", "start"}, "phonics"));
            questions.add(new FillQuestion("The frog ", " into the pond.", "jumped", new String[]{"swim", "flew", "ran"}, "phonics"));
            questions.add(new FillQuestion("She ", " the book twice to understand it.", "read", new String[]{"reed", "red", "rid"}, "phonics"));

        } else if (d.contains("vocab")) {
            questions.add(new FillQuestion("A word that means the same as 'happy' is ", ".", "glad", new String[]{"mad", "bad", "sad"}, "vocabulary"));
            questions.add(new FillQuestion("The opposite of 'big' is ", ".", "small", new String[]{"tall", "fast", "soft"}, "vocabulary"));
            questions.add(new FillQuestion("She was very ", " when she won the prize.", "excited", new String[]{"tired", "bored", "quiet"}, "vocabulary"));
            questions.add(new FillQuestion("A young dog is called a ", ".", "puppy", new String[]{"kitten", "cub", "foal"}, "vocabulary"));
            questions.add(new FillQuestion("He showed great ", " by helping others.", "kindness", new String[]{"anger", "sadness", "fear"}, "vocabulary"));
            questions.add(new FillQuestion("The leaves turn ", " in autumn.", "colorful", new String[]{"heavy", "wet", "silent"}, "vocabulary"));
            questions.add(new FillQuestion("She has a very ", " memory for facts.", "sharp", new String[]{"slow", "weak", "dim"}, "vocabulary"));
            questions.add(new FillQuestion("The word 'enormous' means very ", ".", "big", new String[]{"small", "long", "old"}, "vocabulary"));
            questions.add(new FillQuestion("A ", " is a place where books are kept.", "library", new String[]{"hospital", "market", "factory"}, "vocabulary"));
            questions.add(new FillQuestion("The sky is ", " on a clear day.", "blue", new String[]{"green", "red", "black"}, "vocabulary"));

        } else if (d.contains("grammar")) {
            questions.add(new FillQuestion("She ", " to school every morning.", "goes", new String[]{"go", "gone", "going"}, "grammar"));
            questions.add(new FillQuestion("They ", " playing football yesterday.", "were", new String[]{"are", "is", "was"}, "grammar"));
            questions.add(new FillQuestion("He ", " finished his homework already.", "has", new String[]{"have", "had", "is"}, "grammar"));
            questions.add(new FillQuestion("The children ", " very happy today.", "are", new String[]{"is", "was", "am"}, "grammar"));
            questions.add(new FillQuestion("I ", " a new book last week.", "bought", new String[]{"buy", "buying", "buys"}, "grammar"));
            questions.add(new FillQuestion("She sings ", " in the choir.", "beautifully", new String[]{"beautiful", "beauty", "beautify"}, "grammar"));
            questions.add(new FillQuestion("He runs ", " than his brother.", "faster", new String[]{"fast", "fastest", "fastly"}, "grammar"));
            questions.add(new FillQuestion("The cat and the dog ", " friends.", "are", new String[]{"is", "was", "am"}, "grammar"));
            questions.add(new FillQuestion("We ", " three apples left.", "have", new String[]{"has", "had", "is"}, "grammar"));
            questions.add(new FillQuestion("She ", " eat vegetables every day.", "should", new String[]{"would", "could", "might"}, "grammar"));

        } else if (d.contains("compreh")) {
            questions.add(new FillQuestion("The ", " of a story tells you what it is about.", "topic", new String[]{"setting", "character", "author"}, "comprehending"));
            questions.add(new FillQuestion("Details in the story ", " the main idea.", "support", new String[]{"ignore", "change", "replace"}, "comprehending"));
            questions.add(new FillQuestion("The ", " is where and when the story takes place.", "setting", new String[]{"plot", "theme", "character"}, "comprehending"));
            questions.add(new FillQuestion("The ", " of a story is the lesson it teaches.", "theme", new String[]{"plot", "setting", "conflict"}, "comprehending"));
            questions.add(new FillQuestion("You can ", " what will happen next from story clues.", "predict", new String[]{"read", "write", "ignore"}, "comprehending"));
            questions.add(new FillQuestion("The beginning of a story usually ", " the characters.", "introduces", new String[]{"ends", "changes", "hides"}, "comprehending"));
            questions.add(new FillQuestion("When you ", ", you tell the story in your own words.", "retell", new String[]{"copy", "memorize", "skip"}, "comprehending"));
            questions.add(new FillQuestion("Good readers make ", " while they read.", "inferences", new String[]{"mistakes", "drawings", "lists"}, "comprehending"));
            questions.add(new FillQuestion("A character's ", " tells you what kind of person they are.", "traits", new String[]{"clothes", "height", "name"}, "comprehending"));
            questions.add(new FillQuestion("The ", " of a paragraph is usually the first sentence.", "main idea", new String[]{"detail", "example", "question"}, "comprehending"));

        } else {
            // Creating / Composing (default)
            questions.add(new FillQuestion("A paragraph should begin with a ", " sentence.", "topic", new String[]{"closing", "detail", "support"}, "creating"));
            questions.add(new FillQuestion("Good writing uses ", " words that paint a picture.", "descriptive", new String[]{"short", "simple", "boring"}, "creating"));
            questions.add(new FillQuestion("A story needs a beginning, ", ", and end.", "middle", new String[]{"problem", "character", "setting"}, "creating"));
            questions.add(new FillQuestion("Sentences should start with a ", " letter.", "capital", new String[]{"small", "bold", "colored"}, "creating"));
            questions.add(new FillQuestion("Put a ", " at the end of a question.", "question mark", new String[]{"period", "comma", "apostrophe"}, "creating"));
            questions.add(new FillQuestion("Joining two sentences with 'and' or 'but' makes a ", " sentence.", "compound", new String[]{"simple", "complex", "short"}, "creating"));
            questions.add(new FillQuestion("Good writers ", " their work to fix mistakes.", "proofread", new String[]{"delete", "copy", "skip"}, "creating"));
            questions.add(new FillQuestion("Using ", " makes your writing more interesting.", "adjectives", new String[]{"numbers", "symbols", "colors"}, "creating"));
            questions.add(new FillQuestion("The ", " sentence wraps up the paragraph.", "concluding", new String[]{"topic", "detail", "opening"}, "creating"));
            questions.add(new FillQuestion("A ", " is a group of sentences about one idea.", "paragraph", new String[]{"chapter", "word", "sentence"}, "creating"));
        }

        // Shuffle and return requested count
        Collections.shuffle(questions);
        return questions.subList(0, Math.min(count, questions.size()));
    }

// ==================== ASSESSMENT QUESTIONS DATA ====================



    public static List<com.example.literise.models.Question> getAssessmentQuestions() {

        List<com.example.literise.models.Question> questions = new ArrayList<>();



        // Grammar Questions

        com.example.literise.models.Question q1 = new com.example.literise.models.Question();

        q1.setItemId(2001);

        q1.setItemType("Grammar");

        q1.setQuestionText("Choose the correct verb form: She ___ to school every day.");

        q1.setOptionA("go");

        q1.setOptionB("goes");

        q1.setOptionC("going");

        q1.setOptionD("gone");

        q1.setCorrectOption("B");

        q1.setDifficulty(0.5f);

        q1.setDiscrimination(1.0f);

        q1.setMCQ(true);

        questions.add(q1);



        com.example.literise.models.Question q2 = new com.example.literise.models.Question();

        q2.setItemId(2002);

        q2.setItemType("Grammar");

        q2.setQuestionText("Which sentence is correct?");

        q2.setOptionA("He don't like vegetables");

        q2.setOptionB("He doesn't likes vegetables");

        q2.setOptionC("He doesn't like vegetables");

        q2.setOptionD("He not like vegetables");

        q2.setCorrectOption("C");

        q2.setDifficulty(0.6f);

        q2.setDiscrimination(1.0f);

        q2.setMCQ(true);

        questions.add(q2);



        // Spelling Questions

        com.example.literise.models.Question q3 = new com.example.literise.models.Question();

        q3.setItemId(2003);

        q3.setItemType("Spelling");

        q3.setQuestionText("Choose the correctly spelled word:");

        q3.setOptionA("recieve");

        q3.setOptionB("receive");

        q3.setOptionC("recive");

        q3.setOptionD("receeve");

        q3.setCorrectOption("B");

        q3.setDifficulty(0.7f);

        q3.setDiscrimination(1.0f);

        q3.setMCQ(true);

        questions.add(q3);



        com.example.literise.models.Question q4 = new com.example.literise.models.Question();

        q4.setItemId(2004);

        q4.setItemType("Spelling");

        q4.setQuestionText("Which word is spelled correctly?");

        q4.setOptionA("occassion");

        q4.setOptionB("occasion");

        q4.setOptionC("ocasion");

        q4.setOptionD("ocassion");

        q4.setCorrectOption("B");

        q4.setDifficulty(0.8f);

        q4.setDiscrimination(1.0f);

        q4.setMCQ(true);

        questions.add(q4);



        // Syntax Questions

        com.example.literise.models.Question q5 = new com.example.literise.models.Question();

        q5.setItemId(2005);

        q5.setItemType("Syntax");

        q5.setQuestionText("Arrange these words to form a correct sentence:");

        q5.setScrambledWords(Arrays.asList("dog", "the", "quickly", "ran"));

        q5.setOptionA("The dog ran quickly");

        q5.setOptionB("Quickly the dog ran");

        q5.setOptionC("Ran the dog quickly");

        q5.setOptionD("The quickly dog ran");

        q5.setCorrectOption("A");

        q5.setDifficulty(0.6f);

        q5.setDiscrimination(1.0f);

        q5.setMCQ(true);

        questions.add(q5);



        com.example.literise.models.Question q6 = new com.example.literise.models.Question();

        q6.setItemId(2006);

        q6.setItemType("Syntax");

        q6.setQuestionText("Arrange these words to form a correct sentence:");

        q6.setScrambledWords(Arrays.asList("reading", "enjoys", "books", "she"));

        q6.setOptionA("She enjoys reading books");

        q6.setOptionB("Books reading she enjoys");

        q6.setOptionC("Reading she enjoys books");

        q6.setOptionD("Enjoys she reading books");

        q6.setCorrectOption("A");

        q6.setDifficulty(0.7f);

        q6.setDiscrimination(1.0f);

        q6.setMCQ(true);

        questions.add(q6);



        // More Grammar Questions

        com.example.literise.models.Question q7 = new com.example.literise.models.Question();

        q7.setItemId(2007);

        q7.setItemType("Grammar");

        q7.setQuestionText("Complete the sentence: They ___ playing basketball yesterday.");

        q7.setOptionA("is");

        q7.setOptionB("are");

        q7.setOptionC("was");

        q7.setOptionD("were");

        q7.setCorrectOption("D");

        q7.setDifficulty(0.6f);

        q7.setDiscrimination(1.0f);

        q7.setMCQ(true);

        questions.add(q7);



        com.example.literise.models.Question q8 = new com.example.literise.models.Question();

        q8.setItemId(2008);

        q8.setItemType("Grammar");

        q8.setQuestionText("Choose the correct sentence:");

        q8.setOptionA("I has finished my homework");

        q8.setOptionB("I have finished my homework");

        q8.setOptionC("I finished have my homework");

        q8.setOptionD("I have finish my homework");

        q8.setCorrectOption("B");

        q8.setDifficulty(0.7f);

        q8.setDiscrimination(1.0f);

        q8.setMCQ(true);

        questions.add(q8);



        // Advanced Questions

        com.example.literise.models.Question q9 = new com.example.literise.models.Question();

        q9.setItemId(2009);

        q9.setItemType("Grammar");

        q9.setQuestionText("Identify the sentence with correct punctuation:");

        q9.setOptionA("Where are you going");

        q9.setOptionB("Where are you going.");

        q9.setOptionC("Where are you going?");

        q9.setOptionD("Where are you going,");

        q9.setCorrectOption("C");

        q9.setDifficulty(0.9f);

        q9.setDiscrimination(1.0f);

        q9.setMCQ(true);

        questions.add(q9);



        com.example.literise.models.Question q10 = new com.example.literise.models.Question();

        q10.setItemId(2010);

        q10.setItemType("Spelling");

        q10.setQuestionText("Choose the correctly spelled word:");

        q10.setOptionA("definately");

        q10.setOptionB("definitly");

        q10.setOptionC("definitely");

        q10.setOptionD("definetely");

        q10.setCorrectOption("C");

        q10.setDifficulty(1.0f);

        q10.setDiscrimination(1.0f);

        q10.setMCQ(true);

        questions.add(q10);





        // Pronunciation Question (Speak-type)

        com.example.literise.models.Question q11 = new com.example.literise.models.Question();



        q11.setItemId(2011);



        q11.setItemType("Pronunciation");



        q11.setItemText("education"); // Word to pronounce







        q11.setPassageText("ed-ju-kei-shun"); // Phonetic guide (without slashes)







        q11.setDefinition("The process of receiving or giving systematic instruction, especially at a school or university.");



        q11.setDifficulty(0.7f);



        q11.setDiscrimination(1.0f);



        q11.setMCQ(false); // This is a speak-type pronunciation question



        questions.add(q11);



        return questions;



    }

}