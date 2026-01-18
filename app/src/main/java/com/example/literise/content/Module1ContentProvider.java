package com.example.literise.content;

import com.example.literise.models.Lesson;
import com.example.literise.models.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Content provider for Module 1: Phonics and Word Study (EN3PWS)
 * Based on MATATAG Grade 3 English Curriculum
 *
 * Competencies covered:
 * - Recognize and read high-frequency words
 * - Apply knowledge of sounds and symbols
 * - Decode multisyllabic words
 * - Read with fluency and accuracy
 *
 * Structure: 15 lessons in 3 tiers
 * - Tier 1 (Lessons 1-5): Foundation - Sight words and basic patterns
 * - Tier 2 (Lessons 6-10): Intermediate - Multi-syllabic words
 * - Tier 3 (Lessons 11-15): Advanced - Reading fluency
 */
public class Module1ContentProvider {

    private static final int MODULE_ID = 1;

    /**
     * Get all 15 lessons for Module 1
     */
    public static List<Lesson> getAllLessons() {
        List<Lesson> lessons = new ArrayList<>();

        // Tier 1: Foundation (Lessons 1-5)
        lessons.add(createLesson1());
        lessons.add(createLesson2());
        lessons.add(createLesson3());
        lessons.add(createLesson4());
        lessons.add(createLesson5());

        // Tier 2: Intermediate (Lessons 6-10)
        lessons.add(createLesson6());
        lessons.add(createLesson7());
        lessons.add(createLesson8());
        lessons.add(createLesson9());
        lessons.add(createLesson10());

        // Tier 3: Advanced (Lessons 11-15)
        lessons.add(createLesson11());
        lessons.add(createLesson12());
        lessons.add(createLesson13());
        lessons.add(createLesson14());
        lessons.add(createLesson15());

        return lessons;
    }

    // ==================== TIER 1: FOUNDATION ====================

    /**
     * Lesson 1: High-Frequency Sight Words (Set 1)
     */
    private static Lesson createLesson1() {
        Lesson lesson = new Lesson(101, MODULE_ID, 1,
                "Sight Words: The Basics",
                "Foundation",
                "Learn to recognize and read common sight words instantly");

        // DEBUG: Log before setting game type
        android.util.Log.d("Module1Provider", "Creating Lesson 1 - Before setGameType: " + lesson.getGameType());

        lesson.setGameType(Lesson.GAME_WORD_HUNT); // Fun word search game!

        // DEBUG: Log after setting game type
        android.util.Log.d("Module1Provider", "Creating Lesson 1 - After setGameType: " + lesson.getGameType());

        lesson.setXpReward(20);

        String content = "# Sight Words: The Basics\n\n" +
                "## What are Sight Words?\n" +
                "Sight words are common words that you should recognize instantly without sounding them out. " +
                "Learning these words helps you read faster and more smoothly!\n\n" +
                "## Today's Sight Words:\n" +
                "- **the** - the dog, the cat, the book\n" +
                "- **and** - mom and dad, you and me\n" +
                "- **is** - she is happy, it is big\n" +
                "- **to** - go to school, talk to friends\n" +
                "- **in** - in the house, in my bag\n" +
                "- **it** - it is fun, I like it\n" +
                "- **you** - you are nice, I see you\n" +
                "- **of** - cup of water, bag of toys\n\n" +
                "## Practice Tips:\n" +
                "1. Look at each word carefully\n" +
                "2. Say the word out loud\n" +
                "3. Use the word in a sentence\n" +
                "4. Practice reading the words quickly\n\n" +
                "## Remember:\n" +
                "The more you practice, the faster you'll recognize these words!";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson1Practice());
        lesson.setQuizQuestions(createLesson1Quiz());

        return lesson;
    }

    private static List<Question> createLesson1Practice() {
        List<Question> questions = new ArrayList<>();

        // Question 1
        Question q1 = new Question();
        q1.setItemId(1011);
        q1.setQuestionText("Which word completes this sentence?\n\n___ dog is brown.");
        q1.setOptionA("The");
        q1.setOptionB("And");
        q1.setOptionC("Is");
        q1.setOptionD("To");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        // Question 2
        Question q2 = new Question();
        q2.setItemId(1012);
        q2.setQuestionText("Find the sight word: 'I like cats ___ dogs.'");
        q2.setOptionA("to");
        q2.setOptionB("and");
        q2.setOptionC("in");
        q2.setOptionD("of");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        // Question 3
        Question q3 = new Question();
        q3.setItemId(1013);
        q3.setQuestionText("Which word means 'inside'?");
        q3.setOptionA("it");
        q3.setOptionB("you");
        q3.setOptionC("in");
        q3.setOptionD("of");
        q3.setCorrectOption("C");
        q3.setMCQ(true);
        questions.add(q3);

        // Question 4
        Question q4 = new Question();
        q4.setItemId(1014);
        q4.setQuestionText("Complete: 'The cat ___ on the mat.'");
        q4.setOptionA("and");
        q4.setOptionB("is");
        q4.setOptionC("to");
        q4.setOptionD("you");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        // Question 5
        Question q5 = new Question();
        q5.setItemId(1015);
        q5.setQuestionText("Which word fits? 'I go ___ school.'");
        q5.setOptionA("in");
        q5.setOptionB("of");
        q5.setOptionC("to");
        q5.setOptionD("and");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        // Question 6
        Question q6 = new Question();
        q6.setItemId(1016);
        q6.setQuestionText("Find the correct sight word: '___ are my friend.'");
        q6.setOptionA("It");
        q6.setOptionB("You");
        q6.setOptionC("The");
        q6.setOptionD("In");
        q6.setCorrectOption("B");
        q6.setMCQ(true);
        questions.add(q6);

        // Question 7
        Question q7 = new Question();
        q7.setItemId(1017);
        q7.setQuestionText("Which word completes: 'a glass ___ milk'?");
        q7.setOptionA("to");
        q7.setOptionB("in");
        q7.setOptionC("of");
        q7.setOptionD("is");
        q7.setCorrectOption("C");
        q7.setMCQ(true);
        questions.add(q7);

        // Question 8
        Question q8 = new Question();
        q8.setItemId(1018);
        q8.setQuestionText("Read: 'The book is on the table.' How many sight words?");
        q8.setOptionA("3");
        q8.setOptionB("4");
        q8.setOptionC("5");
        q8.setOptionD("6");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        // Question 9
        Question q9 = new Question();
        q9.setItemId(1019);
        q9.setQuestionText("Which sentence uses 'it' correctly?");
        q9.setOptionA("It is sunny today.");
        q9.setOptionB("I go it school.");
        q9.setOptionC("The it dog.");
        q9.setOptionD("And it the cat.");
        q9.setCorrectOption("A");
        q9.setMCQ(true);
        questions.add(q9);

        // Question 10
        Question q10 = new Question();
        q10.setItemId(1020);
        q10.setQuestionText("Complete the sentence: 'The apple ___ red ___ sweet.'");
        q10.setOptionA("is, and");
        q10.setOptionB("and, is");
        q10.setOptionC("to, in");
        q10.setOptionD("of, you");
        q10.setCorrectOption("A");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson1Quiz() {
        List<Question> questions = new ArrayList<>();

        // Quiz Question 1
        Question q1 = new Question();
        q1.setItemId(1021);
        q1.setQuestionText("Read this sentence and choose the correct word:\n\n'___ book is ___ the table.'");
        q1.setOptionA("The, on");
        q1.setOptionB("And, to");
        q1.setOptionC("Is, of");
        q1.setOptionD("You, it");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        // Quiz Question 2
        Question q2 = new Question();
        q2.setItemId(1022);
        q2.setQuestionText("Which sentence is written correctly?");
        q2.setOptionA("The dog and the cat is playing.");
        q2.setOptionB("I go to the store.");
        q2.setOptionC("You in the house.");
        q2.setOptionD("It of the book.");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        // Quiz Question 3
        Question q3 = new Question();
        q3.setItemId(1023);
        q3.setQuestionText("Count the sight words: 'You and I go to the park. It is fun!'");
        q3.setOptionA("5");
        q3.setOptionB("6");
        q3.setOptionC("7");
        q3.setOptionD("8");
        q3.setCorrectOption("C");
        q3.setMCQ(true);
        questions.add(q3);

        // Quiz Question 4
        Question q4 = new Question();
        q4.setItemId(1024);
        q4.setQuestionText("Fill in the blanks: 'The toy is ___ the box. ___ is mine.'");
        q4.setOptionA("in, It");
        q4.setOptionB("to, You");
        q4.setOptionC("of, And");
        q4.setOptionD("and, The");
        q4.setCorrectOption("A");
        q4.setMCQ(true);
        questions.add(q4);

        // Quiz Question 5
        Question q5 = new Question();
        q5.setItemId(1025);
        q5.setQuestionText("Which word does NOT belong in the sentence?\n\n'The cat and dog of playing in the yard.'");
        q5.setOptionA("and");
        q5.setOptionB("of");
        q5.setOptionC("the");
        q5.setOptionD("in");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 2: High-Frequency Sight Words (Set 2)
     */
    private static Lesson createLesson2() {
        Lesson lesson = new Lesson(102, MODULE_ID, 2,
                "More Sight Words",
                "Foundation",
                "Expand your sight word vocabulary with new common words");
        lesson.setGameType(Lesson.GAME_WORD_HUNT);
        lesson.setXpReward(20);

        String content = "# More Sight Words\n\n" +
                "## New Sight Words to Learn:\n" +
                "- **a** - a book, a dog, a friend\n" +
                "- **was** - it was fun, she was happy\n" +
                "- **he** - he is tall, he runs fast\n" +
                "- **for** - for you, look for it\n" +
                "- **on** - on the table, turn it on\n" +
                "- **are** - they are nice, we are friends\n" +
                "- **as** - as big as, run as fast\n" +
                "- **with** - go with me, play with toys\n\n" +
                "## Using These Words:\n" +
                "These words help us describe when things happened (was), " +
                "who is doing something (he), and how things are connected (with, for, on).\n\n" +
                "## Practice Sentences:\n" +
                "- He was on a bike.\n" +
                "- A cat is with a dog.\n" +
                "- They are as happy as can be!\n" +
                "- This gift is for you.";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson2Practice());
        lesson.setQuizQuestions(createLesson2Quiz());

        return lesson;
    }

    private static List<Question> createLesson2Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1031);
        q1.setQuestionText("Complete: 'There is ___ apple on the table.'");
        q1.setOptionA("a");
        q1.setOptionB("was");
        q1.setOptionC("for");
        q1.setOptionD("with");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1032);
        q2.setQuestionText("Which word shows past tense? 'It ___ raining yesterday.'");
        q2.setOptionA("is");
        q2.setOptionB("was");
        q2.setOptionC("are");
        q2.setOptionD("on");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1033);
        q3.setQuestionText("Which word refers to a boy or man?");
        q3.setOptionA("she");
        q3.setOptionB("it");
        q3.setOptionC("he");
        q3.setOptionD("for");
        q3.setCorrectOption("C");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1034);
        q4.setQuestionText("'This gift is ___ you.' Which word fits?");
        q4.setOptionA("on");
        q4.setOptionB("as");
        q4.setOptionC("with");
        q4.setOptionD("for");
        q4.setCorrectOption("D");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1035);
        q5.setQuestionText("Complete: 'The book is ___ the desk.'");
        q5.setOptionA("for");
        q5.setOptionB("on");
        q5.setOptionC("as");
        q5.setOptionD("was");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1036);
        q6.setQuestionText("'We ___ going to the park.' Which word?");
        q6.setOptionA("is");
        q6.setOptionB("was");
        q6.setOptionC("are");
        q6.setOptionD("on");
        q6.setCorrectOption("C");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1037);
        q7.setQuestionText("Complete: 'I am ___ tall ___ my brother.'");
        q7.setOptionA("as, as");
        q7.setOptionB("on, on");
        q7.setOptionC("for, for");
        q7.setOptionD("with, with");
        q7.setCorrectOption("A");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1038);
        q8.setQuestionText("'Come ___ me to the store.' Which word?");
        q8.setOptionA("as");
        q8.setOptionB("was");
        q8.setOptionC("for");
        q8.setOptionD("with");
        q8.setCorrectOption("D");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1039);
        q9.setQuestionText("Which sentence uses 'he' correctly?");
        q9.setOptionA("He is my friend.");
        q9.setOptionB("The he runs.");
        q9.setOptionC("He for the book.");
        q9.setOptionD("On he table.");
        q9.setCorrectOption("A");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1040);
        q10.setQuestionText("How many sight words? 'He was on a bike with a friend.'");
        q10.setOptionA("5");
        q10.setOptionB("6");
        q10.setOptionC("7");
        q10.setOptionD("8");
        q10.setCorrectOption("C");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson2Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1041);
        q1.setQuestionText("Complete the sentence: '___ cat ___ sleeping ___ the chair.'");
        q1.setOptionA("A, was, on");
        q1.setOptionB("He, are, for");
        q1.setOptionC("With, is, as");
        q1.setOptionD("For, on, was");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1042);
        q2.setQuestionText("Which sentence is correct?");
        q2.setOptionA("He are going to school.");
        q2.setOptionB("She was happy yesterday.");
        q2.setOptionC("They is on the bus.");
        q2.setOptionD("We was playing.");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1043);
        q3.setQuestionText("Fill in: 'This book is ___ Tom. ___ will read it.'");
        q3.setOptionA("for, He");
        q3.setOptionB("with, She");
        q3.setOptionC("on, It");
        q3.setOptionD("as, We");
        q3.setCorrectOption("A");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1044);
        q4.setQuestionText("Count the NEW sight words from Lesson 2:\n\n'He was playing with a ball on the grass.'");
        q4.setOptionA("3");
        q4.setOptionB("4");
        q4.setOptionC("5");
        q4.setOptionD("6");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1045);
        q5.setQuestionText("Which word does NOT fit?\n\n'They are running as fast ___ they can.'");
        q5.setOptionA("as");
        q5.setOptionB("on");
        q5.setOptionC("for");
        q5.setOptionD("with");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 3: Simple CVC Word Patterns (Consonant-Vowel-Consonant)
     */
    private static Lesson createLesson3() {
        Lesson lesson = new Lesson(103, MODULE_ID, 3,
                "CVC Words: Cat, Bat, Mat",
                "Foundation",
                "Learn to decode simple three-letter words");
        lesson.setGameType(Lesson.GAME_SENTENCE_SCRAMBLE);
        lesson.setXpReward(20);

        String content = "# CVC Words: Simple Three-Letter Words\n\n" +
                "## What are CVC Words?\n" +
                "CVC stands for Consonant-Vowel-Consonant. These are simple three-letter words " +
                "that follow an easy pattern!\n\n" +
                "## The Pattern:\n" +
                "🔵 Consonant (b, c, d, f, etc.)\n" +
                "🔴 Vowel (a, e, i, o, u)\n" +
                "🔵 Consonant (t, n, p, etc.)\n\n" +
                "## Common CVC Words:\n" +
                "### -at family\n" +
                "- cat, bat, mat, rat, hat, sat, pat, fat\n\n" +
                "### -an family\n" +
                "- can, man, ran, pan, fan, van, tan, ban\n\n" +
                "### -og family\n" +
                "- dog, log, fog, hog, jog, bog\n\n" +
                "### -ig family\n" +
                "- big, dig, fig, pig, wig, jig\n\n" +
                "## Decoding Strategy:\n" +
                "1. Look at the first letter and say its sound\n" +
                "2. Add the vowel sound in the middle\n" +
                "3. Add the last consonant sound\n" +
                "4. Blend all three sounds together!\n\n" +
                "Example: c-a-t → cat 🐱";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson3Practice());
        lesson.setQuizQuestions(createLesson3Quiz());

        return lesson;
    }

    private static List<Question> createLesson3Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1051);
        q1.setQuestionText("Which word belongs to the '-at' family?");
        q1.setOptionA("cat");
        q1.setOptionB("dog");
        q1.setOptionC("pig");
        q1.setOptionD("run");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1052);
        q2.setQuestionText("Complete the word: 'The _at sat on the mat.' (rhymes with bat)");
        q2.setOptionA("c");
        q2.setOptionB("d");
        q2.setOptionC("f");
        q2.setOptionD("l");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1053);
        q3.setQuestionText("Which word rhymes with 'man'?");
        q3.setOptionA("mat");
        q3.setOptionB("can");
        q3.setOptionC("cat");
        q3.setOptionD("dog");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1054);
        q4.setQuestionText("What is the vowel in the word 'dog'?");
        q4.setOptionA("d");
        q4.setOptionB("o");
        q4.setOptionC("g");
        q4.setOptionD("none");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1055);
        q5.setQuestionText("Which word fits? 'The _ig is pink.' (a farm animal)");
        q5.setOptionA("dog");
        q5.setOptionB("cat");
        q5.setOptionC("pig");
        q5.setOptionD("bat");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1056);
        q6.setQuestionText("All these words are CVC words EXCEPT:");
        q6.setOptionA("cat");
        q6.setOptionB("dog");
        q6.setOptionC("tree");
        q6.setOptionD("big");
        q6.setCorrectOption("C");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1057);
        q7.setQuestionText("What word do these sounds make? j-o-g");
        q7.setOptionA("jog");
        q7.setOptionB("jag");
        q7.setOptionC("jig");
        q7.setOptionD("jug");
        q7.setCorrectOption("A");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1058);
        q8.setQuestionText("Which word rhymes with 'wig'?");
        q8.setOptionA("big");
        q8.setOptionB("wag");
        q8.setOptionC("wig");
        q8.setOptionD("web");
        q8.setCorrectOption("A");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1059);
        q9.setQuestionText("Complete: 'The man has a ___.' (rhymes with 'pan')");
        q9.setOptionA("cat");
        q9.setOptionB("dog");
        q9.setOptionC("fan");
        q9.setOptionD("bat");
        q9.setCorrectOption("C");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1060);
        q10.setQuestionText("How many CVC words? 'The cat and the dog ran to the big log.'");
        q10.setOptionA("2");
        q10.setOptionB("3");
        q10.setOptionC("4");
        q10.setOptionD("5");
        q10.setCorrectOption("C");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson3Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1061);
        q1.setQuestionText("Read: 'A fat cat sat on a mat.' How many '-at' words?");
        q1.setOptionA("2");
        q1.setOptionB("3");
        q1.setOptionC("4");
        q1.setOptionD("5");
        q1.setCorrectOption("C");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1062);
        q2.setQuestionText("Which word does NOT rhyme with the others?");
        q2.setOptionA("man");
        q2.setOptionB("can");
        q2.setOptionC("ran");
        q2.setOptionD("cat");
        q2.setCorrectOption("D");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1063);
        q3.setQuestionText("Blend these sounds: b-i-g");
        q3.setOptionA("bag");
        q3.setOptionB("big");
        q3.setOptionC("bug");
        q3.setOptionD("beg");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1064);
        q4.setQuestionText("Complete the sentence with a CVC word:\n\n'The ___ likes to bark.'");
        q4.setOptionA("tree");
        q4.setOptionB("house");
        q4.setOptionC("dog");
        q4.setOptionD("animal");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1065);
        q5.setQuestionText("Which sentence uses CVC words correctly?");
        q5.setOptionA("A big pig can dig.");
        q5.setOptionB("The beautiful butterfly flies.");
        q5.setOptionC("Yesterday was wonderful.");
        q5.setOptionD("The elephant is enormous.");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 4: Word Families and Rhyming
     */
    private static Lesson createLesson4() {
        Lesson lesson = new Lesson(104, MODULE_ID, 4,
                "Word Families and Rhymes",
                "Foundation",
                "Identify rhyming words and word families");
        lesson.setGameType(Lesson.GAME_SENTENCE_SCRAMBLE);
        lesson.setXpReward(20);

        String content = "# Word Families and Rhyming\n\n" +
                "## What are Word Families?\n" +
                "Word families are groups of words that have the same ending sound and letters.\n\n" +
                "## Common Word Families:\n\n" +
                "### -ack family 🎒\n" +
                "back, pack, sack, rack, track, black, snack\n\n" +
                "### -ell family 🔔\n" +
                "bell, tell, sell, well, shell, spell, smell\n\n" +
                "### -ill family ⛰️\n" +
                "hill, will, fill, kill, pill, still, spill\n\n" +
                "### -ock family 🔒\n" +
                "lock, rock, sock, dock, clock, block, knock\n\n" +
                "### -ump family\n" +
                "jump, bump, pump, dump, lump, stump, grump\n\n" +
                "## Why Rhyming is Important:\n" +
                "- Helps you read new words faster\n" +
                "- Makes reading more fun\n" +
                "- Helps you spell words correctly\n\n" +
                "## Tip:\n" +
                "If you can read 'back', you can read 'pack', 'sack', and 'track'!";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson4Practice());
        lesson.setQuizQuestions(createLesson4Quiz());

        return lesson;
    }

    private static List<Question> createLesson4Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1071);
        q1.setQuestionText("Which word rhymes with 'bell'?");
        q1.setOptionA("ball");
        q1.setOptionB("well");
        q1.setOptionC("bill");
        q1.setOptionD("bull");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1072);
        q2.setQuestionText("Complete: 'I packed my ___.' (rhymes with back)");
        q2.setOptionA("bag");
        q2.setOptionB("sack");
        q2.setOptionC("book");
        q2.setOptionD("lunch");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1073);
        q3.setQuestionText("Which word belongs to the -ill family?");
        q3.setOptionA("hill");
        q3.setOptionB("hello");
        q3.setOptionC("help");
        q3.setOptionD("hole");
        q3.setCorrectOption("A");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1074);
        q4.setQuestionText("What rhymes with 'clock'?");
        q4.setOptionA("click");
        q4.setOptionB("cluck");
        q4.setOptionC("rock");
        q4.setOptionD("cloak");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1075);
        q5.setQuestionText("Find the word that does NOT rhyme: jump, bump, lamp, pump");
        q5.setOptionA("jump");
        q5.setOptionB("bump");
        q5.setOptionC("lamp");
        q5.setOptionD("pump");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1076);
        q6.setQuestionText("Complete the sentence: 'I can ___ you a story.' (rhymes with bell)");
        q6.setOptionA("tell");
        q6.setOptionB("talk");
        q6.setOptionC("say");
        q6.setOptionD("give");
        q6.setCorrectOption("A");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1077);
        q7.setQuestionText("Which pair rhymes?");
        q7.setOptionA("sock - rock");
        q7.setOptionB("sock - sick");
        q7.setOptionC("sock - sack");
        q7.setOptionD("sock - seek");
        q7.setCorrectOption("A");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1078);
        q8.setQuestionText("How many words rhyme with 'black'? back, bark, sack, rack");
        q8.setOptionA("1");
        q8.setOptionB("2");
        q8.setOptionC("3");
        q8.setOptionD("4");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1079);
        q9.setQuestionText("Complete: 'Don't ___ the milk!' (rhymes with hill)");
        q9.setOptionA("drop");
        q9.setOptionB("spill");
        q9.setOptionC("pour");
        q9.setOptionD("drink");
        q9.setCorrectOption("B");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1080);
        q10.setQuestionText("Which word family? well, shell, smell, tell");
        q10.setOptionA("-all");
        q10.setOptionB("-ell");
        q10.setOptionC("-ill");
        q10.setOptionD("-oll");
        q10.setCorrectOption("B");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson4Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1081);
        q1.setQuestionText("Read this poem and count the rhyming words:\n\n'Jack fell down the hill. Jill will get help. Jack is still on the ground.'");
        q1.setOptionA("2");
        q1.setOptionB("3");
        q1.setOptionC("4");
        q1.setOptionD("5");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1082);
        q2.setQuestionText("Which sentence has the most rhyming words?");
        q2.setOptionA("The clock on the rock by the dock.");
        q2.setOptionB("The cat and the dog play together.");
        q2.setOptionC("I like to read books.");
        q2.setOptionD("Today is a sunny day.");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1083);
        q3.setQuestionText("Complete with rhyming words: 'I will climb the ___ to get the ___.'");
        q3.setOptionA("tree, leaf");
        q3.setOptionB("hill, pill");
        q3.setOptionC("mountain, water");
        q3.setOptionD("stairs, chair");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1084);
        q4.setQuestionText("Find the word that breaks the rhyme pattern:\n\nback, sack, rack, rock, pack");
        q4.setOptionA("back");
        q4.setOptionB("sack");
        q4.setOptionC("rock");
        q4.setOptionD("pack");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1085);
        q5.setQuestionText("Which word completes both sentences?\n\n'I ___ jump high.' 'Where there is a ___, there is a way.'");
        q5.setOptionA("can");
        q5.setOptionB("will");
        q5.setOptionC("should");
        q5.setOptionD("might");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 5: Beginning and Ending Sounds
     */
    private static Lesson createLesson5() {
        Lesson lesson = new Lesson(105, MODULE_ID, 5,
                "Beginning and Ending Sounds",
                "Foundation",
                "Master initial and final consonant sounds");
        lesson.setGameType(Lesson.GAME_WORD_HUNT);
        lesson.setXpReward(25);

        String content = "# Beginning and Ending Sounds\n\n" +
                "## Why This Matters:\n" +
                "Understanding the first and last sounds in words helps you:\n" +
                "- Sound out new words\n" +
                "- Spell words correctly\n" +
                "- Read more fluently\n\n" +
                "## Common Beginning Sounds:\n" +
                "- **b** as in: ball, book, bird 🏀📚🐦\n" +
                "- **c** as in: cat, car, cup 🐱🚗☕\n" +
                "- **d** as in: dog, door, duck 🐕🚪🦆\n" +
                "- **f** as in: fish, fan, frog 🐟🪭🐸\n" +
                "- **s** as in: sun, sat, sock ☀️💺🧦\n\n" +
                "## Common Ending Sounds:\n" +
                "- **-t** as in: cat, bat, sit\n" +
                "- **-n** as in: sun, can, run\n" +
                "- **-p** as in: cup, mop, hop\n" +
                "- **-g** as in: dog, log, big\n" +
                "- **-d** as in: mad, sad, bed\n\n" +
                "## Practice Strategy:\n" +
                "1. Say the word slowly\n" +
                "2. Listen for the first sound\n" +
                "3. Listen for the last sound\n" +
                "4. Write or identify the letter!";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson5Practice());
        lesson.setQuizQuestions(createLesson5Quiz());

        return lesson;
    }

    private static List<Question> createLesson5Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1091);
        q1.setQuestionText("What is the beginning sound of 'dog'?");
        q1.setOptionA("b");
        q1.setOptionB("d");
        q1.setOptionC("g");
        q1.setOptionD("o");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1092);
        q2.setQuestionText("What is the ending sound of 'cat'?");
        q2.setOptionA("c");
        q2.setOptionB("a");
        q2.setOptionC("t");
        q2.setOptionD("k");
        q2.setCorrectOption("C");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1093);
        q3.setQuestionText("Which word starts with 'f'?");
        q3.setOptionA("sun");
        q3.setOptionB("fish");
        q3.setOptionC("cat");
        q3.setOptionD("dog");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1094);
        q4.setQuestionText("Which word ends with 'n'?");
        q4.setOptionA("sun");
        q4.setOptionB("cat");
        q4.setOptionC("dog");
        q4.setOptionD("cup");
        q4.setCorrectOption("A");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1095);
        q5.setQuestionText("All these words start with the same sound EXCEPT:");
        q5.setOptionA("ball");
        q5.setOptionB("book");
        q5.setOptionC("bird");
        q5.setOptionD("sun");
        q5.setCorrectOption("D");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1096);
        q6.setQuestionText("Which word has the same ending sound as 'cup'?");
        q6.setOptionA("cat");
        q6.setOptionB("mop");
        q6.setOptionC("can");
        q6.setOptionD("car");
        q6.setCorrectOption("B");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1097);
        q7.setQuestionText("What beginning sound is shared by: car, cat, cup?");
        q7.setOptionA("b");
        q7.setOptionB("c");
        q7.setOptionC("d");
        q7.setOptionD("s");
        q7.setCorrectOption("B");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1098);
        q8.setQuestionText("Which word ends with 'g'?");
        q8.setOptionA("cat");
        q8.setOptionB("sun");
        q8.setOptionC("log");
        q8.setOptionD("cup");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1099);
        q9.setQuestionText("Find the word with 's' at the beginning and 't' at the end:");
        q9.setOptionA("sun");
        q9.setOptionB("sit");
        q9.setOptionC("sat");
        q9.setOptionD("set");
        q9.setCorrectOption("B");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1100);
        q10.setQuestionText("Which pair of words starts with the same sound?");
        q10.setOptionA("dog, cat");
        q10.setOptionB("fish, fun");
        q10.setOptionC("sun, moon");
        q10.setOptionD("ball, cup");
        q10.setCorrectOption("B");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson5Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1101);
        q1.setQuestionText("Read: 'The dog sat on a log.' \n\nHow many words end with 'g'?");
        q1.setOptionA("1");
        q1.setOptionB("2");
        q1.setOptionC("3");
        q1.setOptionD("4");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1102);
        q2.setQuestionText("Which word starts AND ends with the same letter as 'dad'?");
        q2.setOptionA("dud");
        q2.setOptionB("dog");
        q2.setOptionC("did");
        q2.setOptionD("day");
        q2.setCorrectOption("C");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1103);
        q3.setQuestionText("Count words starting with 'b': 'The ball bounced by the big bush.'");
        q3.setOptionA("2");
        q3.setOptionB("3");
        q3.setOptionC("4");
        q3.setOptionD("5");
        q3.setCorrectOption("C");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1104);
        q4.setQuestionText("Which sentence has words with matching ending sounds?");
        q4.setOptionA("The cat is fat and sat on the mat.");
        q4.setOptionB("I like to play outside every day.");
        q4.setOptionC("The dog ran to the park.");
        q4.setOptionD("She reads books in the morning.");
        q4.setCorrectOption("A");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1105);
        q5.setQuestionText("Find the word where the beginning and ending sounds are both consonants:");
        q5.setOptionA("apple");
        q5.setOptionB("egg");
        q5.setOptionC("dog");
        q5.setOptionD("ice");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    // ==================== TIER 2: INTERMEDIATE ====================

    /**
     * Lesson 6: Consonant Blends (bl, cr, st, etc.)
     */
    private static Lesson createLesson6() {
        Lesson lesson = new Lesson(106, MODULE_ID, 6,
                "Consonant Blends",
                "Intermediate",
                "Learn to blend two consonants at the start of words");
        lesson.setGameType(Lesson.GAME_SENTENCE_SCRAMBLE);
        lesson.setXpReward(25);

        String content = "# Consonant Blends\n\n" +
                "## What are Consonant Blends?\n" +
                "Consonant blends are two or more consonants that come together, " +
                "but you can still hear each sound!\n\n" +
                "## Common Beginning Blends:\n\n" +
                "### L-blends:\n" +
                "- **bl** - black, blue, blow, block\n" +
                "- **cl** - clap, clean, close, climb\n" +
                "- **fl** - flag, fly, floor, flower\n" +
                "- **gl** - glad, glue, glass, glow\n" +
                "- **pl** - play, please, plant, plus\n" +
                "- **sl** - slow, sleep, slide, slip\n\n" +
                "### R-blends:\n" +
                "- **br** - brown, bring, brick, break\n" +
                "- **cr** - crab, cry, crash, cream\n" +
                "- **dr** - drum, dress, drive, drop\n" +
                "- **fr** - frog, free, from, fruit\n" +
                "- **gr** - green, grow, grass, grape\n" +
                "- **tr** - tree, train, truck, track\n\n" +
                "### S-blends:\n" +
                "- **sc** - scare, scale, scout\n" +
                "- **sk** - sky, skip, skill, skate\n" +
                "- **sm** - small, smile, smell, smart\n" +
                "- **sn** - snake, snow, snap, snack\n" +
                "- **sp** - spin, spot, speak, spoon\n" +
                "- **st** - stop, star, stand, stay\n" +
                "- **sw** - swim, sweet, swing, switch\n\n" +
                "## Remember:\n" +
                "In a blend, you hear BOTH sounds quickly together: b-l-ack = black!";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson6Practice());
        lesson.setQuizQuestions(createLesson6Quiz());

        return lesson;
    }

    private static List<Question> createLesson6Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1111);
        q1.setQuestionText("Which word starts with a consonant blend?");
        q1.setOptionA("cat");
        q1.setOptionB("stop");
        q1.setOptionC("dog");
        q1.setOptionD("sun");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1112);
        q2.setQuestionText("What blend do you hear at the start of 'flag'?");
        q2.setOptionA("fl");
        q2.setOptionB("fr");
        q2.setOptionC("bl");
        q2.setOptionD("cl");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1113);
        q3.setQuestionText("Which word has the 'tr' blend?");
        q3.setOptionA("tree");
        q3.setOptionB("three");
        q3.setOptionC("tear");
        q3.setOptionD("tier");
        q3.setCorrectOption("A");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1114);
        q4.setQuestionText("Complete: 'The ___ is in the sky.' (st- blend)");
        q4.setOptionA("sun");
        q4.setOptionB("star");
        q4.setOptionC("moon");
        q4.setOptionD("cloud");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1115);
        q5.setQuestionText("Which pair of words both start with blends?");
        q5.setOptionA("black, blue");
        q5.setOptionB("cat, dog");
        q5.setOptionC("apple, egg");
        q5.setOptionD("sun, moon");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1116);
        q6.setQuestionText("What blend is in the word 'crab'?");
        q6.setOptionA("br");
        q6.setOptionB("cr");
        q6.setOptionC("dr");
        q6.setOptionD("tr");
        q6.setCorrectOption("B");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1117);
        q7.setQuestionText("Which is an S-blend word?");
        q7.setOptionA("swim");
        q7.setOptionB("sum");
        q7.setOptionC("same");
        q7.setOptionD("some");
        q7.setCorrectOption("A");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1118);
        q8.setQuestionText("Complete: 'I ___ my hands.' (cl- blend)");
        q8.setOptionA("wash");
        q8.setOptionB("clap");
        q8.setOptionC("clean");
        q8.setOptionD("Both B and C");
        q8.setCorrectOption("D");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1119);
        q9.setQuestionText("How many words have blends? 'The green frog can jump and swim.'");
        q9.setOptionA("1");
        q9.setOptionB("2");
        q9.setOptionC("3");
        q9.setOptionD("4");
        q9.setCorrectOption("B");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1120);
        q10.setQuestionText("Which word does NOT have a consonant blend?");
        q10.setOptionA("stop");
        q10.setOptionB("step");
        q10.setOptionC("slip");
        q10.setOptionD("ship");
        q10.setCorrectOption("D");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson6Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1121);
        q1.setQuestionText("Read: 'A small snake sleeps under the green grass.'\n\nHow many blend words?");
        q1.setOptionA("3");
        q1.setOptionB("4");
        q1.setOptionC("5");
        q1.setOptionD("6");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1122);
        q2.setQuestionText("Which sentence has the most consonant blends?");
        q2.setOptionA("The black crow flies to the tree.");
        q2.setOptionB("A dog runs in the park.");
        q2.setOptionC("The sun is hot today.");
        q2.setOptionD("I like to eat apples.");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1123);
        q3.setQuestionText("Complete with blend words: 'The ___ drops from the ___.'");
        q3.setOptionA("rain, cloud");
        q3.setOptionB("snow, sky");
        q3.setOptionC("water, glass");
        q3.setOptionD("fruit, tree");
        q3.setCorrectOption("D");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1124);
        q4.setQuestionText("Find all the L-blend words: flag, from, fly, flower, fall");
        q4.setOptionA("2");
        q4.setOptionB("3");
        q4.setOptionC("4");
        q4.setOptionD("5");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1125);
        q5.setQuestionText("Which word has both a beginning blend AND an ending blend?");
        q5.setOptionA("black");
        q5.setOptionB("plant");
        q5.setOptionC("stop");
        q5.setOptionD("bring");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    // Continue with remaining lessons (7-15)...
    // For brevity, I'll create placeholder methods that follow the same structure

    /**
     * Lesson 7: Digraphs (sh, ch, th, wh)
     */
    private static Lesson createLesson7() {
        Lesson lesson = new Lesson(107, MODULE_ID, 7,
                "Digraphs: sh, ch, th, wh",
                "Intermediate",
                "Learn consonant combinations that make one sound");
        lesson.setGameType(Lesson.GAME_WORD_HUNT);
        lesson.setXpReward(25);

        String content = "# Consonant Digraphs\n\n" +
                "## What are Digraphs?\n" +
                "Digraphs are TWO letters that work together to make ONE new sound!\n\n" +
                "## The Four Main Digraphs:\n\n" +
                "### sh - /sh/ sound\n" +
                "- ship, shop, fish, wash, brush, shell\n" +
                "- Can be at the beginning OR end of words!\n\n" +
                "### ch - /ch/ sound\n" +
                "- chair, chip, lunch, beach, much, teach\n" +
                "- Like when you sneeze: a-CHOO!\n\n" +
                "### th - /th/ sound\n" +
                "- this, that, bath, tooth, mother, think\n" +
                "- Put your tongue between your teeth!\n\n" +
                "### wh - /wh/ sound\n" +
                "- what, when, where, why, which, white\n" +
                "- Question words often start with 'wh'!\n\n" +
                "## Important!\n" +
                "Unlike blends (where you hear both sounds), in digraphs you hear ONE NEW sound!\n\n" +
                "Example:\n" +
                "- Blend: 'st' in stop = you hear s + t\n" +
                "- Digraph: 'sh' in shop = you hear /sh/ (NOT s + h)";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson7Practice());
        lesson.setQuizQuestions(createLesson7Quiz());
        return lesson;
    }

    private static List<Question> createLesson7Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1131);
        q1.setQuestionText("Which word has the 'sh' digraph?");
        q1.setOptionA("stop");
        q1.setOptionB("shop");
        q1.setOptionC("spot");
        q1.setOptionD("step");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1132);
        q2.setQuestionText("Complete: '___ is your name?' (wh- digraph)");
        q2.setOptionA("What");
        q2.setOptionB("Who");
        q2.setOptionC("When");
        q2.setOptionD("All of these");
        q2.setCorrectOption("D");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1133);
        q3.setQuestionText("Which word ends with 'ch'?");
        q3.setOptionA("lunch");
        q3.setOptionB("lump");
        q3.setOptionC("land");
        q3.setOptionD("long");
        q3.setCorrectOption("A");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1134);
        q4.setQuestionText("Find the 'th' word:");
        q4.setOptionA("tree");
        q4.setOptionB("three");
        q4.setOptionC("trim");
        q4.setOptionD("trap");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1135);
        q5.setQuestionText("Which word has a digraph at the END?");
        q5.setOptionA("fish");
        q5.setOptionB("from");
        q5.setOptionC("fast");
        q5.setOptionD("frog");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1136);
        q6.setQuestionText("Complete: 'I like to ___ in the pool.' (digraph word)");
        q6.setOptionA("swim");
        q6.setOptionB("splash");
        q6.setOptionC("slide");
        q6.setOptionD("skip");
        q6.setCorrectOption("B");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1137);
        q7.setQuestionText("Which is a 'ch' word?");
        q7.setOptionA("chair");
        q7.setOptionB("care");
        q7.setOptionC("car");
        q7.setOptionD("chop");
        q7.setCorrectOption("D");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1138);
        q8.setQuestionText("How many digraph words? 'I wish to catch three fish.'");
        q8.setOptionA("2");
        q8.setOptionB("3");
        q8.setOptionC("4");
        q8.setOptionD("5");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1139);
        q9.setQuestionText("Which word does NOT have a digraph?");
        q9.setOptionA("ship");
        q9.setOptionB("skip");
        q9.setOptionC("shop");
        q9.setOptionD("fish");
        q9.setCorrectOption("B");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1140);
        q10.setQuestionText("Complete: 'Brush your ___.' (th- digraph)");
        q10.setOptionA("hair");
        q10.setOptionB("teeth");
        q10.setOptionC("hands");
        q10.setOptionD("face");
        q10.setCorrectOption("B");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson7Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1141);
        q1.setQuestionText("Read: 'What time should we meet at the beach?'\n\nHow many digraphs?");
        q1.setOptionA("2");
        q1.setOptionB("3");
        q1.setOptionC("4");
        q1.setOptionD("5");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1142);
        q2.setQuestionText("Which sentence has the most digraph words?");
        q2.setOptionA("The fish swims in the shop.");
        q2.setOptionB("She chose the white chair with charm.");
        q2.setOptionC("I can run and jump fast.");
        q2.setOptionD("The dog likes to play.");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1143);
        q3.setQuestionText("Complete with digraph words: 'We ___ eat lunch at the ___ table.'");
        q3.setOptionA("will, big");
        q3.setOptionB("should, white");
        q3.setOptionC("can, round");
        q3.setOptionD("might, small");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1144);
        q4.setQuestionText("Find the word with TWO digraphs:");
        q4.setOptionA("ship");
        q4.setOptionB("fish");
        q4.setOptionC("chop");
        q4.setOptionD("shush");
        q4.setCorrectOption("D");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1145);
        q5.setQuestionText("Which word pair both have ending digraphs?");
        q5.setOptionA("fish, dish");
        q5.setOptionB("shop, ship");
        q5.setOptionC("when, what");
        q5.setOptionD("chair, chop");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 8: Long Vowel Patterns (Magic E)
     */
    private static Lesson createLesson8() {
        Lesson lesson = new Lesson(108, MODULE_ID, 8,
                "Long Vowel Patterns",
                "Intermediate",
                "Understand long vowel sounds with silent e");
        lesson.setGameType(Lesson.GAME_SENTENCE_SCRAMBLE);
        lesson.setXpReward(25);

        String content = "# Long Vowel Sounds: The Magic E!\n\n" +
                "## What is the Magic E?\n" +
                "When you add an 'e' at the end of some words, " +
                "it makes the vowel say its NAME instead of its sound!\n\n" +
                "## Short vs Long Vowels:\n\n" +
                "### a → a (says 'ay')\n" +
                "- cap → cape\n" +
                "- hat → hate\n" +
                "- mad → made\n" +
                "- pan → pane\n\n" +
                "### i → i (says 'eye')\n" +
                "- bit → bite\n" +
                "- pin → pine\n" +
                "- kit → kite\n" +
                "- rip → ripe\n\n" +
                "### o → o (says 'oh')\n" +
                "- hop → hope\n" +
                "- not → note\n" +
                "- rob → robe\n" +
                "- mop → mope\n\n" +
                "### u → u (says 'you')\n" +
                "- cub → cube\n" +
                "- tub → tube\n" +
                "- cut → cute\n" +
                "- us → use\n\n" +
                "## The Pattern:\n" +
                "consonant + vowel + consonant + **e**\n" +
                "The 'e' is silent, but it changes the vowel sound!\n\n" +
                "## Example:\n" +
                "- can (short 'a') → cane (long 'a')";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson8Practice());
        lesson.setQuizQuestions(createLesson8Quiz());
        return lesson;
    }

    private static List<Question> createLesson8Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1151);
        q1.setQuestionText("Add magic 'e': cap → ___");
        q1.setOptionA("cape");
        q1.setOptionB("caps");
        q1.setOptionC("caped");
        q1.setOptionD("caper");
        q1.setCorrectOption("A");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1152);
        q2.setQuestionText("Which word has a LONG vowel sound?");
        q2.setOptionA("bit");
        q2.setOptionB("bite");
        q2.setOptionC("sit");
        q2.setOptionD("fit");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1153);
        q3.setQuestionText("Complete: 'I fly my ___ in the park.' (magic e word)");
        q3.setOptionA("kit");
        q3.setOptionB("kite");
        q3.setOptionC("kitten");
        q3.setOptionD("king");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1154);
        q4.setQuestionText("Which word does NOT have a magic e?");
        q4.setOptionA("hope");
        q4.setOptionB("note");
        q4.setOptionC("home");
        q4.setOptionD("stop");
        q4.setCorrectOption("D");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1155);
        q5.setQuestionText("Add 'e': tub → ___");
        q5.setOptionA("tube");
        q5.setOptionB("tuba");
        q5.setOptionC("tubs");
        q5.setOptionD("tubby");
        q5.setCorrectOption("A");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1156);
        q6.setQuestionText("Which pair shows short/long vowel?");
        q6.setOptionA("hat/hate");
        q6.setOptionB("cat/bat");
        q6.setOptionC("dog/log");
        q6.setOptionD("run/fun");
        q6.setCorrectOption("A");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1157);
        q7.setQuestionText("Complete: 'The baby is so ___!' (magic e)");
        q7.setOptionA("cut");
        q7.setOptionB("cute");
        q7.setOptionC("cub");
        q7.setOptionD("cup");
        q7.setCorrectOption("B");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1158);
        q8.setQuestionText("How many magic e words? 'I hope to make a cake and take it home.'");
        q8.setOptionA("3");
        q8.setOptionB("4");
        q8.setOptionC("5");
        q8.setOptionD("6");
        q8.setCorrectOption("B");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1159);
        q9.setQuestionText("Which word means 'become older'?");
        q9.setOptionA("age");
        q9.setOptionB("ag");
        q9.setOptionC("aged");
        q9.setOptionD("aging");
        q9.setCorrectOption("A");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1160);
        q10.setQuestionText("Remove the 'e': robe → ___");
        q10.setOptionA("rob");
        q10.setOptionB("robs");
        q10.setOptionC("robber");
        q10.setOptionD("robed");
        q10.setCorrectOption("A");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson8Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1161);
        q1.setQuestionText("Read: 'I made a cute cake and ate it at home.'\n\nHow many magic e words?");
        q1.setOptionA("3");
        q1.setOptionB("4");
        q1.setOptionC("5");
        q1.setOptionD("6");
        q1.setCorrectOption("C");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1162);
        q2.setQuestionText("Which sentence uses ONLY long vowel words?");
        q2.setOptionA("The cat sat on the mat.");
        q2.setOptionB("I hope to bake a cake.");
        q2.setOptionC("The dog and cat run.");
        q2.setOptionD("A big pig can dig.");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1163);
        q3.setQuestionText("Complete: 'Put the ice ___ in the ___.' (both magic e)");
        q3.setOptionA("box, bag");
        q3.setOptionB("cube, tube");
        q3.setOptionC("can, cup");
        q3.setOptionD("block, tub");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1164);
        q4.setQuestionText("Find the word pair where adding 'e' changes the meaning:");
        q4.setOptionA("hop/hope");
        q4.setOptionB("stop/step");
        q4.setOptionC("jump/jumped");
        q4.setOptionD("run/runs");
        q4.setCorrectOption("A");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1165);
        q5.setQuestionText("Which word does NOT follow the magic e pattern?");
        q5.setOptionA("make");
        q5.setOptionB("take");
        q5.setOptionC("have");
        q5.setOptionD("lake");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 9: Two-Syllable Words
     */
    private static Lesson createLesson9() {
        Lesson lesson = new Lesson(109, MODULE_ID, 9,
                "Two-Syllable Words",
                "Intermediate",
                "Break words into syllables for easier reading");
        lesson.setGameType(Lesson.GAME_WORD_HUNT);
        lesson.setXpReward(30);

        String content = "# Two-Syllable Words\n\n" +
                "## What is a Syllable?\n" +
                "A syllable is a part of a word that has ONE vowel sound. " +
                "You can clap out syllables!\n\n" +
                "## Examples of 2-Syllable Words:\n\n" +
                "### Nature words:\n" +
                "- rab-bit 🐰\n" +
                "- kit-ten 🐱\n" +
                "- pup-py 🐶\n" +
                "- tur-tle 🐢\n\n" +
                "### People words:\n" +
                "- ba-by 👶\n" +
                "- sis-ter 👧\n" +
                "- teach-er 👩‍🏫\n" +
                "- doc-tor 👨‍⚕️\n\n" +
                "### Things words:\n" +
                "- pen-cil ✏️\n" +
                "- ta-ble 🪑\n" +
                "- win-dow 🪟\n" +
                "- pil-low 🛏️\n\n" +
                "## How to Read Them:\n" +
                "1. Find the two parts\n" +
                "2. Read the first part\n" +
                "3. Read the second part\n" +
                "4. Put them together!\n\n" +
                "## Try it:\n" +
                "hap-py → happy!\n" +
                "bas-ket → basket!\n" +
                "pic-nic → picnic!";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson9Practice());
        lesson.setQuizQuestions(createLesson9Quiz());
        return lesson;
    }

    private static List<Question> createLesson9Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1171);
        q1.setQuestionText("How many syllables in 'rabbit'?");
        q1.setOptionA("1");
        q1.setOptionB("2");
        q1.setOptionC("3");
        q1.setOptionD("4");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1172);
        q2.setQuestionText("Which word has 2 syllables?");
        q2.setOptionA("cat");
        q2.setOptionB("kitten");
        q2.setOptionC("dog");
        q2.setOptionD("big");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1173);
        q3.setQuestionText("Break this word: basket → ___");
        q3.setOptionA("ba-sket");
        q3.setOptionB("bas-ket");
        q3.setOptionC("bask-et");
        q3.setOptionD("b-asket");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1174);
        q4.setQuestionText("Which word has only 1 syllable?");
        q4.setOptionA("happy");
        q4.setOptionB("teacher");
        q4.setOptionC("book");
        q4.setOptionD("rabbit");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1175);
        q5.setQuestionText("Complete: A young cat is a ___.");
        q5.setOptionA("cat");
        q5.setOptionB("kitten");
        q5.setOptionC("dog");
        q5.setOptionD("puppy");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1176);
        q6.setQuestionText("How do you break 'window'?");
        q6.setOptionA("wi-ndow");
        q6.setOptionB("wind-ow");
        q6.setOptionC("win-dow");
        q6.setOptionD("w-indow");
        q6.setCorrectOption("C");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1177);
        q7.setQuestionText("Which is a 2-syllable word?");
        q7.setOptionA("jump");
        q7.setOptionB("jumping");
        q7.setOptionC("run");
        q7.setOptionD("hop");
        q7.setCorrectOption("B");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1178);
        q8.setQuestionText("Count 2-syllable words: 'The happy kitten and puppy play together.'");
        q8.setOptionA("2");
        q8.setOptionB("3");
        q8.setOptionC("4");
        q8.setOptionD("5");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1179);
        q9.setQuestionText("Which word means 'a young dog'?");
        q9.setOptionA("dog");
        q9.setOptionB("puppy");
        q9.setOptionC("baby");
        q9.setOptionD("kitten");
        q9.setCorrectOption("B");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1180);
        q10.setQuestionText("Break: teacher → ___");
        q10.setOptionA("tea-cher");
        q10.setOptionB("teach-er");
        q10.setOptionC("te-acher");
        q10.setOptionD("t-eacher");
        q10.setCorrectOption("B");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson9Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1181);
        q1.setQuestionText("Read: 'The teacher gave the children pencils for the lesson.'\n\nHow many 2-syllable words?");
        q1.setOptionA("2");
        q1.setOptionB("3");
        q1.setOptionC("4");
        q1.setOptionD("5");
        q1.setCorrectOption("C");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1182);
        q2.setQuestionText("Which sentence has the most 2-syllable words?");
        q2.setOptionA("The rabbit and turtle are friends.");
        q2.setOptionB("I like to eat.");
        q2.setOptionC("The big dog runs.");
        q2.setOptionD("A cat sat.");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1183);
        q3.setQuestionText("Complete with 2-syllable words: 'My ___ likes to play with her ___.'");
        q3.setOptionA("cat, toy");
        q3.setOptionB("sister, kitten");
        q3.setOptionC("dog, ball");
        q3.setOptionD("friend, pet");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1184);
        q4.setQuestionText("Find the word that has 3 syllables:");
        q4.setOptionA("happy");
        q4.setOptionB("rabbit");
        q4.setOptionC("beautiful");
        q4.setOptionD("kitten");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1185);
        q5.setQuestionText("Which word pair are both 2-syllable words?");
        q5.setOptionA("cat, dog");
        q5.setOptionB("table, window");
        q5.setOptionC("run, jump");
        q5.setOptionD("big, small");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    /**
     * Lesson 10: Compound Words
     */
    private static Lesson createLesson10() {
        Lesson lesson = new Lesson(110, MODULE_ID, 10,
                "Compound Words",
                "Intermediate",
                "Combine two words to make a new word");
        lesson.setGameType(Lesson.GAME_SENTENCE_SCRAMBLE);
        lesson.setXpReward(30);

        String content = "# Compound Words\n\n" +
                "## What is a Compound Word?\n" +
                "A compound word is made by putting TWO smaller words together!\n\n" +
                "## Common Compound Words:\n\n" +
                "### Nature compounds:\n" +
                "- sun + flower = sunflower 🌻\n" +
                "- rain + bow = rainbow 🌈\n" +
                "- snow + man = snowman ⛄\n" +
                "- butter + fly = butterfly 🦋\n\n" +
                "### Place compounds:\n" +
                "- bed + room = bedroom 🛏️\n" +
                "- class + room = classroom 🏫\n" +
                "- bath + room = bathroom 🚿\n" +
                "- play + ground = playground 🛝\n\n" +
                "### Action compounds:\n" +
                "- foot + ball = football ⚽\n" +
                "- basket + ball = basketball 🏀\n" +
                "- home + work = homework 📚\n" +
                "- tooth + brush = toothbrush 🪥\n\n" +
                "### Time compounds:\n" +
                "- day + time = daytime ☀️\n" +
                "- night + time = nighttime 🌙\n" +
                "- birth + day = birthday 🎂\n" +
                "- week + end = weekend 📅\n\n" +
                "## Tips for Reading:\n" +
                "1. Look for two small words\n" +
                "2. Read the first word\n" +
                "3. Read the second word\n" +
                "4. Put them together!\n\n" +
                "Example: cup + cake = cupcake 🧁";

        lesson.setContent(content);
        lesson.setPracticeQuestions(createLesson10Practice());
        lesson.setQuizQuestions(createLesson10Quiz());
        return lesson;
    }

    private static List<Question> createLesson10Practice() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1191);
        q1.setQuestionText("Which is a compound word?");
        q1.setOptionA("flower");
        q1.setOptionB("sunflower");
        q1.setOptionC("beautiful");
        q1.setOptionD("garden");
        q1.setCorrectOption("B");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1192);
        q2.setQuestionText("sun + glasses = ___");
        q2.setOptionA("sunny");
        q2.setOptionB("sunglasses");
        q2.setOptionC("glass");
        q2.setOptionD("sunshine");
        q2.setCorrectOption("B");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1193);
        q3.setQuestionText("Break this compound: rainbow → ___");
        q3.setOptionA("rain + bow");
        q3.setOptionB("ra + inbow");
        q3.setOptionC("rai + nbow");
        q3.setOptionD("It's not compound");
        q3.setCorrectOption("A");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1194);
        q4.setQuestionText("foot + ball = ___");
        q4.setOptionA("feet");
        q4.setOptionB("football");
        q4.setOptionC("ball");
        q4.setOptionD("foot");
        q4.setCorrectOption("B");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1195);
        q5.setQuestionText("Which is NOT a compound word?");
        q5.setOptionA("bedroom");
        q5.setOptionB("bathroom");
        q5.setOptionC("kitchen");
        q5.setOptionD("classroom");
        q5.setCorrectOption("C");
        q5.setMCQ(true);
        questions.add(q5);

        Question q6 = new Question();
        q6.setItemId(1196);
        q6.setQuestionText("tooth + brush = ___");
        q6.setOptionA("teeth");
        q6.setOptionB("brush");
        q6.setOptionC("toothbrush");
        q6.setOptionD("brushing");
        q6.setCorrectOption("C");
        q6.setMCQ(true);
        questions.add(q6);

        Question q7 = new Question();
        q7.setItemId(1197);
        q7.setQuestionText("What two words make 'snowman'?");
        q7.setOptionA("snow + man");
        q7.setOptionB("sno + wman");
        q7.setOptionC("s + nowman");
        q7.setOptionD("snowy + man");
        q7.setCorrectOption("A");
        q7.setMCQ(true);
        questions.add(q7);

        Question q8 = new Question();
        q8.setItemId(1198);
        q8.setQuestionText("Count compound words: 'We saw a butterfly and rainbow at the playground.'");
        q8.setOptionA("1");
        q8.setOptionB("2");
        q8.setOptionC("3");
        q8.setOptionD("4");
        q8.setCorrectOption("C");
        q8.setMCQ(true);
        questions.add(q8);

        Question q9 = new Question();
        q9.setItemId(1199);
        q9.setQuestionText("cup + cake = ___");
        q9.setOptionA("cup");
        q9.setOptionB("cake");
        q9.setOptionC("cupcake");
        q9.setOptionD("cakes");
        q9.setCorrectOption("C");
        q9.setMCQ(true);
        questions.add(q9);

        Question q10 = new Question();
        q10.setItemId(1200);
        q10.setQuestionText("Which pair can make a compound word?");
        q10.setOptionA("home + work");
        q10.setOptionB("big + dog");
        q10.setOptionC("red + car");
        q10.setOptionD("happy + cat");
        q10.setCorrectOption("A");
        q10.setMCQ(true);
        questions.add(q10);

        return questions;
    }

    private static List<Question> createLesson10Quiz() {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setItemId(1201);
        q1.setQuestionText("Read: 'On my birthday, I played football in the playground.'\n\nHow many compound words?");
        q1.setOptionA("1");
        q1.setOptionB("2");
        q1.setOptionC("3");
        q1.setOptionD("4");
        q1.setCorrectOption("C");
        q1.setMCQ(true);
        questions.add(q1);

        Question q2 = new Question();
        q2.setItemId(1202);
        q2.setQuestionText("Which sentence has the most compound words?");
        q2.setOptionA("I saw a butterfly and sunflower in the daytime.");
        q2.setOptionB("The cat is big and happy.");
        q2.setOptionC("We like to run and play.");
        q2.setOptionD("The dog barks loudly.");
        q2.setCorrectOption("A");
        q2.setMCQ(true);
        questions.add(q2);

        Question q3 = new Question();
        q3.setItemId(1203);
        q3.setQuestionText("Make a compound: 'I do my ___ in my ___.'");
        q3.setOptionA("work, room");
        q3.setOptionB("homework, bedroom");
        q3.setOptionC("lessons, house");
        q3.setOptionD("reading, place");
        q3.setCorrectOption("B");
        q3.setMCQ(true);
        questions.add(q3);

        Question q4 = new Question();
        q4.setItemId(1204);
        q4.setQuestionText("Find the word that is NOT a compound:");
        q4.setOptionA("basketball");
        q4.setOptionB("baseball");
        q4.setOptionC("soccer");
        q4.setOptionD("football");
        q4.setCorrectOption("C");
        q4.setMCQ(true);
        questions.add(q4);

        Question q5 = new Question();
        q5.setItemId(1205);
        q5.setQuestionText("Create a compound: What do you brush your teeth with?");
        q5.setOptionA("brush");
        q5.setOptionB("toothbrush");
        q5.setOptionC("teeth");
        q5.setOptionD("cleaner");
        q5.setCorrectOption("B");
        q5.setMCQ(true);
        questions.add(q5);

        return questions;
    }

    private static Lesson createLesson11() {
        Lesson lesson = new Lesson(111, MODULE_ID, 11,
                "Reading Fluency Practice",
                "Advanced",
                "Read sentences smoothly and with expression");
        lesson.setGameType(Lesson.GAME_TRADITIONAL);
        lesson.setXpReward(30);
        lesson.setContent("# Reading Fluency\n\nRead smoothly, not word by word!");
        lesson.setPracticeQuestions(new ArrayList<>());
        lesson.setQuizQuestions(new ArrayList<>());
        return lesson;
    }

    private static Lesson createLesson12() {
        Lesson lesson = new Lesson(112, MODULE_ID, 12,
                "Prefixes and Suffixes",
                "Advanced",
                "Learn word parts that change meaning");
        lesson.setGameType(Lesson.GAME_TRADITIONAL);
        lesson.setXpReward(30);
        lesson.setContent("# Prefixes and Suffixes\n\nAdd parts to words to change their meaning!");
        lesson.setPracticeQuestions(new ArrayList<>());
        lesson.setQuizQuestions(new ArrayList<>());
        return lesson;
    }

    private static Lesson createLesson13() {
        Lesson lesson = new Lesson(113, MODULE_ID, 13,
                "Multi-Syllable Word Decoding",
                "Advanced",
                "Read longer words with confidence");
        lesson.setGameType(Lesson.GAME_TRADITIONAL);
        lesson.setXpReward(35);
        lesson.setContent("# Multi-Syllable Words\n\nBreak big words into chunks!");
        lesson.setPracticeQuestions(new ArrayList<>());
        lesson.setQuizQuestions(new ArrayList<>());
        return lesson;
    }

    private static Lesson createLesson14() {
        Lesson lesson = new Lesson(114, MODULE_ID, 14,
                "Context Clues for Unknown Words",
                "Advanced",
                "Use surrounding words to figure out meaning");
        lesson.setGameType(Lesson.GAME_TRADITIONAL);
        lesson.setXpReward(35);
        lesson.setContent("# Context Clues\n\nUse the story to figure out new words!");
        lesson.setPracticeQuestions(new ArrayList<>());
        lesson.setQuizQuestions(new ArrayList<>());
        return lesson;
    }

    private static Lesson createLesson15() {
        Lesson lesson = new Lesson(115, MODULE_ID, 15,
                "Reading with Expression",
                "Advanced",
                "Bring stories to life with your voice");
        lesson.setGameType(Lesson.GAME_TRADITIONAL);
        lesson.setXpReward(40);
        lesson.setContent("# Reading with Expression\n\nMake your reading sound like talking!");
        lesson.setPracticeQuestions(new ArrayList<>());
        lesson.setQuizQuestions(new ArrayList<>());
        return lesson;
    }
}