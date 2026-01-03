package com.example.literise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.literise.models.PlacementQuestion;

import java.util.ArrayList;
import java.util.List;

public class QuestionBankHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "placement_questions.db";
    private static final int DATABASE_VERSION = 2; // Incremented for schema change

    // Table name
    private static final String TABLE_QUESTIONS = "questions";

    // Column names
    private static final String COL_ID = "question_id";
    private static final String COL_CATEGORY = "category";
    private static final String COL_SUBCATEGORY = "subcategory";
    private static final String COL_TYPE = "question_type";
    private static final String COL_TEXT = "question_text";
    private static final String COL_AUDIO = "audio_url";
    private static final String COL_IMAGE = "image_url";
    private static final String COL_READING_PASSAGE = "reading_passage";
    private static final String COL_OPTIONS = "options_json";
    private static final String COL_CORRECT = "correct_answer";
    private static final String COL_DIFFICULTY = "difficulty";
    private static final String COL_DISCRIMINATION = "discrimination";
    private static final String COL_HINT = "leo_hint";

    public QuestionBankHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CATEGORY + " INTEGER, "
                + COL_SUBCATEGORY + " TEXT, "
                + COL_TYPE + " TEXT, "
                + COL_TEXT + " TEXT, "
                + COL_AUDIO + " TEXT, "
                + COL_IMAGE + " TEXT, "
                + COL_READING_PASSAGE + " TEXT, "
                + COL_OPTIONS + " TEXT, "
                + COL_CORRECT + " TEXT, "
                + COL_DIFFICULTY + " REAL, "
                + COL_DISCRIMINATION + " REAL, "
                + COL_HINT + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);

        // Insert sample questions
        insertSampleQuestions(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
        onCreate(db);
    }

    // Insert a question
    public long insertQuestion(PlacementQuestion question) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_CATEGORY, question.getCategory());
        values.put(COL_SUBCATEGORY, question.getSubcategory());
        values.put(COL_TYPE, question.getQuestionType());
        values.put(COL_TEXT, question.getQuestionText());
        values.put(COL_AUDIO, question.getAudioUrl());
        values.put(COL_IMAGE, question.getImageUrl());
        values.put(COL_READING_PASSAGE, question.getReadingPassage());
        values.put(COL_OPTIONS, question.getOptionsAsJson());
        values.put(COL_CORRECT, question.getCorrectAnswer());
        values.put(COL_DIFFICULTY, question.getDifficulty());
        values.put(COL_DISCRIMINATION, question.getDiscrimination());
        values.put(COL_HINT, question.getLeoHint());

        long id = db.insert(TABLE_QUESTIONS, null, values);
        db.close();
        return id;
    }

    // Get questions by category
    public List<PlacementQuestion> getQuestionsByCategory(int category) {
        List<PlacementQuestion> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_QUESTIONS,
                null,
                COL_CATEGORY + "=?",
                new String[]{String.valueOf(category)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                questions.add(extractQuestionFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return questions;
    }

    // Get question by ID
    public PlacementQuestion getQuestionById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        PlacementQuestion question = null;

        Cursor cursor = db.query(TABLE_QUESTIONS,
                null,
                COL_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor.moveToFirst()) {
            question = extractQuestionFromCursor(cursor);
        }

        cursor.close();
        db.close();
        return question;
    }

    // Helper method to extract question from cursor
    private PlacementQuestion extractQuestionFromCursor(Cursor cursor) {
        PlacementQuestion question = new PlacementQuestion();

        question.setQuestionId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        question.setCategory(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        question.setSubcategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBCATEGORY)));
        question.setQuestionType(cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE)));
        question.setQuestionText(cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT)));
        question.setAudioUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_AUDIO)));
        question.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
        question.setReadingPassage(cursor.getString(cursor.getColumnIndexOrThrow(COL_READING_PASSAGE)));

        String optionsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_OPTIONS));
        question.setOptions(PlacementQuestion.parseOptionsFromJson(optionsJson));

        question.setCorrectAnswer(cursor.getString(cursor.getColumnIndexOrThrow(COL_CORRECT)));
        question.setDifficulty(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DIFFICULTY)));
        question.setDiscrimination(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISCRIMINATION)));
        question.setLeoHint(cursor.getString(cursor.getColumnIndexOrThrow(COL_HINT)));

        return question;
    }

    // Insert sample questions for testing
    private void insertSampleQuestions(SQLiteDatabase db) {
        // Category 1: Oral Language - Vocabulary (3 questions)
        insertSampleQuestion(db, 1, "Vocabulary", "multiple_choice",
                "He was too shy to _____ to strangers.",
                null, null, null,
                "[\"talk\",\"talking\",\"talked\",\"be talked\"]",
                "talk", -1.0, 1.2, "Pick the right word!");

        insertSampleQuestion(db, 1, "Vocabulary", "multiple_choice",
                "The cat is _____ on the mat.",
                null, null, null,
                "[\"sleep\",\"sleeping\",\"slept\",\"sleeps\"]",
                "sleeping", -0.5, 1.1, "What is the cat doing right now?");

        insertSampleQuestion(db, 1, "Vocabulary", "multiple_choice",
                "I _____ breakfast every morning.",
                null, null, null,
                "[\"eat\",\"eating\",\"ate\",\"eats\"]",
                "eat", 0.0, 1.3, "Think about what you do every day!");

        // Category 1: Pronunciation Questions (3 questions)
        insertSampleQuestion(db, 1, "Phonological", "pronunciation",
                "cat",
                null, null, null, null,
                "cat", -0.8, 1.1, null);

        insertSampleQuestion(db, 1, "Phonological", "pronunciation",
                "beautiful",
                null, null, null, null,
                "beautiful", 0.5, 1.3, null);

        insertSampleQuestion(db, 1, "Phonological", "pronunciation",
                "school",
                null, null, null, null,
                "school", -0.3, 1.0, null);

        // Category 2: Word Knowledge - Vocabulary (7 questions)
        insertSampleQuestion(db, 2, "Vocabulary", "multiple_choice",
                "A _____ is a place where you borrow books.",
                null, null, null,
                "[\"library\",\"hospital\",\"store\",\"park\"]",
                "library", -0.5, 1.2, "Where do you find lots of books?");

        insertSampleQuestion(db, 2, "Vocabulary", "multiple_choice",
                "Something that is _____ makes you laugh.",
                null, null, null,
                "[\"funny\",\"sad\",\"angry\",\"tired\"]",
                "funny", -1.0, 1.0, "Think about jokes!");

        insertSampleQuestion(db, 2, "Phonics", "multiple_choice",
                "What sound does 'ch' make in 'chair'?",
                null, null, null,
                "[\"ch as in cheese\",\"k as in kite\",\"s as in sun\",\"sh as in ship\"]",
                "ch as in cheese", 0.0, 1.3, "Say it out loud: ch-air!");

        insertSampleQuestion(db, 2, "Phonics", "multiple_choice",
                "Which word has a long 'a' sound?",
                null, null, null,
                "[\"cake\",\"cat\",\"cap\",\"can\"]",
                "cake", 0.3, 1.2, "Listen for 'ay' sound!");

        insertSampleQuestion(db, 2, "Word Study", "multiple_choice",
                "Which is a sight word?",
                null, null, null,
                "[\"the\",\"elephant\",\"butterfly\",\"dinosaur\"]",
                "the", -1.5, 0.9, "Which word do you see most often?");

        insertSampleQuestion(db, 2, "Word Study", "multiple_choice",
                "Which words are in the same family as 'cat'?",
                null, null, null,
                "[\"bat, hat, rat\",\"car, tar, jar\",\"cup, pup, sup\",\"dog, log, fog\"]",
                "bat, hat, rat", 0.2, 1.4, "They all end with 'at'!");

        insertSampleQuestion(db, 2, "Word Study", "multiple_choice",
                "What does 'un-' mean in 'unhappy'?",
                null, null, null,
                "[\"not\",\"very\",\"again\",\"before\"]",
                "not", 0.8, 1.5, "'Un-' makes it opposite!");

        // Category 3: Reading Comprehension (7 questions)
        insertSampleQuestion(db, 3, "Narrative", "multiple_choice",
                "Mia loves to play soccer. She practices every day after school. What does Mia love?",
                null, null, null,
                "[\"Soccer\",\"Basketball\",\"Reading\",\"Drawing\"]",
                "Soccer", -0.8, 1.1, "Read carefully!");

        insertSampleQuestion(db, 3, "Narrative", "multiple_choice",
                "The little bird couldn't fly yet. His mother brought him worms to eat. Why couldn't the bird fly?",
                null, null, null,
                "[\"He was too little\",\"He was scared\",\"He was sleeping\",\"He was eating\"]",
                "He was too little", 0.0, 1.3, "Think about baby birds!");

        insertSampleQuestion(db, 3, "Narrative", "multiple_choice",
                "Sam woke up late. He missed the bus. He had to walk to school. What happened first?",
                null, null, null,
                "[\"Sam woke up late\",\"Sam missed the bus\",\"Sam walked to school\",\"Sam got to school\"]",
                "Sam woke up late", 0.5, 1.4, "What happened at the beginning?");

        insertSampleQuestion(db, 3, "Informational", "multiple_choice",
                "Bees make honey. They collect nectar from flowers. What do bees collect from flowers?",
                null, null, null,
                "[\"Nectar\",\"Water\",\"Pollen\",\"Seeds\"]",
                "Nectar", -0.3, 1.2, "What do they gather?");

        insertSampleQuestion(db, 3, "Informational", "multiple_choice",
                "Plants need water, sunlight, and air to grow. What THREE things do plants need?",
                null, null, null,
                "[\"Water, sunlight, air\",\"Food, toys, games\",\"Rocks, sand, dirt\",\"Books, pens, paper\"]",
                "Water, sunlight, air", 0.2, 1.3, "All three are important!");

        insertSampleQuestion(db, 3, "Informational", "multiple_choice",
                "The sun is a star. It gives us light and heat. What is the sun?",
                null, null, null,
                "[\"A star\",\"A planet\",\"A moon\",\"A cloud\"]",
                "A star", -0.5, 1.0, "Look at the first sentence!");

        insertSampleQuestion(db, 3, "Informational", "multiple_choice",
                "Dolphins are mammals that live in the ocean. They are very smart. Where do dolphins live?",
                null, null, null,
                "[\"Ocean\",\"Forest\",\"Desert\",\"Mountains\"]",
                "Ocean", -0.7, 1.1, "Find it in the text!");

        // Category 3: Reading Questions with Karaoke (2 questions)
        insertSampleQuestion(db, 3, "Narrative", "reading",
                "What did the cat sit on?",
                null, null,
                "The cat sat on the mat. It was a sunny day.",
                "[\"A mat\",\"A chair\",\"A table\",\"A bed\"]",
                "A mat", -0.5, 1.0, null);

        insertSampleQuestion(db, 3, "Narrative", "reading",
                "What color was the bird?",
                null, null,
                "A little blue bird flew to the tree. It sang a happy song.",
                "[\"Blue\",\"Red\",\"Yellow\",\"Green\"]",
                "Blue", -0.3, 1.1, null);

        // Category 4: Language Structure (5 questions)
        insertSampleQuestion(db, 4, "Grammar", "multiple_choice",
                "_____ going to the park today.",
                null, null, null,
                "[\"We're\",\"Were\",\"Where\",\"Wear\"]",
                "We're", 0.5, 1.4, "Which one means 'We are'?");

        insertSampleQuestion(db, 4, "Grammar", "multiple_choice",
                "She _____ her homework yesterday.",
                null, null, null,
                "[\"did\",\"do\",\"does\",\"doing\"]",
                "did", 0.3, 1.2, "It happened yesterday!");

        insertSampleQuestion(db, 4, "Grammar", "multiple_choice",
                "Which sentence is correct?",
                null, null, null,
                "[\"The dogs are playing.\",\"The dogs is playing.\",\"The dog are playing.\",\"The dogs am playing.\"]",
                "The dogs are playing.", 0.7, 1.5, "Match 'dogs' with the right verb!");

        insertSampleQuestion(db, 4, "Sentence Construction", "multiple_choice",
                "Put the words in order: 'loves / she / ice cream'",
                null, null, null,
                "[\"She loves ice cream\",\"Loves she ice cream\",\"Ice cream she loves\",\"She ice cream loves\"]",
                "She loves ice cream", 0.4, 1.3, "Start with who is doing it!");

        insertSampleQuestion(db, 4, "Sentence Construction", "multiple_choice",
                "Which sentence needs a question mark?",
                null, null, null,
                "[\"Where is my book\",\"I like pizza\",\"The sky is blue\",\"Birds can fly\"]",
                "Where is my book", 0.6, 1.4, "Which one is asking something?");
    }

    // Helper method to insert a sample question
    private void insertSampleQuestion(SQLiteDatabase db, int category, String subcategory,
                                     String type, String text, String audio, String image,
                                     String readingPassage, String optionsJson, String correct,
                                     double difficulty, double discrimination, String hint) {
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORY, category);
        values.put(COL_SUBCATEGORY, subcategory);
        values.put(COL_TYPE, type);
        values.put(COL_TEXT, text);
        values.put(COL_AUDIO, audio);
        values.put(COL_IMAGE, image);
        values.put(COL_READING_PASSAGE, readingPassage);
        values.put(COL_OPTIONS, optionsJson);
        values.put(COL_CORRECT, correct);
        values.put(COL_DIFFICULTY, difficulty);
        values.put(COL_DISCRIMINATION, discrimination);
        values.put(COL_HINT, hint);

        db.insert(TABLE_QUESTIONS, null, values);
    }
}
