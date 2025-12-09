package com.example.literise.activities.games;



import android.animation.ArgbEvaluator;

import android.animation.ValueAnimator;

import android.content.Intent;

import android.graphics.Color;

import android.graphics.Paint;

import android.graphics.Typeface;

import android.media.AudioManager;

import android.media.ToneGenerator;

import android.os.Bundle;

import android.os.CountDownTimer;

import android.os.Handler;

import android.os.Looper;

import android.util.TypedValue;

import android.view.Gravity;

import android.view.LayoutInflater;

import android.view.MotionEvent;

import android.view.View;

import android.view.ViewGroup;

import android.widget.GridLayout;

import android.widget.ProgressBar;

import android.widget.TextView;



import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;

import androidx.core.content.ContextCompat;



import com.example.literise.R;

import com.example.literise.api.ApiClient;

import com.example.literise.api.ApiService;

import com.example.literise.database.SessionManager;

import com.example.literise.models.SaveGameResultRequest;

import com.example.literise.models.SaveGameResultResponse;

import com.example.literise.models.WordHuntResponse;

import com.example.literise.models.WordHuntWord;

import com.example.literise.utils.AppConfig;

import com.example.literise.utils.CustomToast;

import com.example.literise.utils.DemoDataProvider;

import com.google.android.flexbox.FlexboxLayout;

import com.google.android.material.button.MaterialButton;



import java.util.ArrayList;

import java.util.HashSet;

import java.util.List;

import java.util.Locale;

import java.util.Random;

import java.util.Set;



import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;



/**

 * Word Hunt Game Activity

 * Find hidden vocabulary words in a letter grid

 */

public class WordHuntActivity extends BaseGameActivity {



    // Views

    private TextView tvTitle, tvProgress, tvTimer, tvScore;

    private TextView tvFoundWord, tvDefinition, tvExample;

    private GridLayout letterGrid;

    private FlexboxLayout wordListContainer;

    private ProgressBar progressBar, timerProgress, loadingProgress;

    private MaterialButton btnHint, btnShuffle;

    private CardView cardDefinition;



    // Game State

    private List<WordHuntWord> words = new ArrayList<>();

    private char[][] grid;

    private TextView[][] gridCells;

    private int gridSize = 10;

    private int wordsFound = 0;

    private int score = 0;

    private int hintsUsed = 0;

    private long startTime;



    // Selection State

    private boolean isSelecting = false;

    private int startRow = -1, startCol = -1;

    private int endRow = -1, endCol = -1;

    private List<int[]> selectedCells = new ArrayList<>();

    private Set<String> highlightedCells = new HashSet<>();



    // Timer

    private CountDownTimer gameTimer;

    private static final long TOTAL_TIME = 120000; // 2 minutes

    private long timeRemaining = TOTAL_TIME;



    // Animation

    private Handler handler = new Handler(Looper.getMainLooper());



    // Session

    private SessionManager session;

    private Integer lessonId;

    private Integer sessionId;



    // Constants

    private static final int POINTS_PER_WORD = 50;

    private static final int BONUS_ALL_WORDS = 200;

    private static final int TIME_BONUS = 100;

    private static final int HINT_PENALTY = 25;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_word_hunt);



        session = new SessionManager(this);

        startTime = System.currentTimeMillis();



        // Get lesson and session info from intent

        Intent intent = getIntent();

        if (intent != null) {

            lessonId = intent.hasExtra("lesson_id") ? intent.getIntExtra("lesson_id", -1) : null;

            sessionId = intent.hasExtra("session_id") ? intent.getIntExtra("session_id", -1) : null;

            if (lessonId != null && lessonId == -1) lessonId = null;

            if (sessionId != null && sessionId == -1) sessionId = null;

        }



        initializeViews();

        setupListeners();

        loadWords();

    }



    private void initializeViews() {

        tvTitle = findViewById(R.id.tvTitle);

        tvProgress = findViewById(R.id.tvProgress);

        tvTimer = findViewById(R.id.tvTimer);

        tvScore = findViewById(R.id.tvScore);

        tvFoundWord = findViewById(R.id.tvFoundWord);

        tvDefinition = findViewById(R.id.tvDefinition);

        tvExample = findViewById(R.id.tvExample);



        letterGrid = findViewById(R.id.letterGrid);

        wordListContainer = findViewById(R.id.wordListContainer);



        progressBar = findViewById(R.id.progressBar);

        timerProgress = findViewById(R.id.timerProgress);

        loadingProgress = findViewById(R.id.loadingProgress);



        btnHint = (MaterialButton) findViewById(R.id.btnHint);



        btnShuffle = (MaterialButton) findViewById(R.id.btnShuffle);



        cardDefinition = (CardView) findViewById(R.id.cardDefinition);

    }



    private void setupListeners() {

        findViewById(R.id.ivClose).setOnClickListener(v -> showExitConfirmation());

        btnHint.setOnClickListener(v -> useHint());

        btnShuffle.setOnClickListener(v -> shuffleGrid());

    }



    private void loadWords() {

        // DEMO MODE: Use hardcoded words directly (no API)

        if (AppConfig.DEMO_MODE) {

            loadDemoWords();

            startGame();

            return;

        }



        loadingProgress.setVisibility(View.VISIBLE);



        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        int studentId = session.getStudentId();



        if (studentId <= 0) {

            loadingProgress.setVisibility(View.GONE);

            showErrorAndExit("No student ID found. Please log in again.");

            return;

        }



        Call<WordHuntResponse> call;

        if (lessonId != null) {

            call = apiService.getWordHuntWords(8, lessonId, studentId);

        } else {

            call = apiService.getWordHuntWords(8, studentId);

        }



        android.util.Log.d("WordHunt", "Loading words for studentId: " + studentId + ", lessonId: " + lessonId);



        call.enqueue(new Callback<WordHuntResponse>() {

            @Override

            public void onResponse(Call<WordHuntResponse> call, Response<WordHuntResponse> response) {

                loadingProgress.setVisibility(View.GONE);



                if (response.isSuccessful() && response.body() != null) {

                    android.util.Log.d("WordHunt", "API Response: success=" + response.body().isSuccess() +

                            ", grade=" + response.body().getStudentGrade() +

                            ", words=" + (response.body().getWords() != null ? response.body().getWords().size() : 0));



                    if (response.body().isSuccess()) {

                        words = response.body().getWords();

                        if (response.body().getGridSize() > 0) {

                            gridSize = response.body().getGridSize();

                        }

                        if (words != null && !words.isEmpty()) {

                            // Log words for verification

                            for (WordHuntWord word : words) {

                                android.util.Log.d("WordHunt", "Word: " + word.getWord() + ", Grade: " + word.getGradeLevel());

                            }

                            startGame();

                        } else {

                            showErrorAndExit("No words found for your grade level. Please contact administrator.");

                        }

                    } else {

                        showErrorAndExit("API Error: " + response.body().getMessage());

                    }

                } else {

                    String errorMsg = "Failed to load words";

                    try {

                        if (response.errorBody() != null) {

                            errorMsg = response.errorBody().string();

                        }

                    } catch (Exception e) {

                        errorMsg = "HTTP " + response.code();

                    }

                    showErrorAndExit("Server Error: " + errorMsg);

                }

            }



            @Override

            public void onFailure(Call<WordHuntResponse> call, Throwable t) {

                loadingProgress.setVisibility(View.GONE);

                android.util.Log.e("WordHunt", "API call failed", t);

                showErrorAndExit("Network Error: " + t.getMessage());

            }

        });

    }



    private void showErrorAndExit(String message) {

        android.util.Log.e("WordHunt", message);

        new AlertDialog.Builder(this)

                .setTitle("Error Loading Game")

                .setMessage(message)

                .setPositiveButton("OK", (dialog, which) -> finish())

                .setCancelable(false)

                .show();

    }



    private void loadDemoWords() {

        words = new ArrayList<>();

        List<DemoDataProvider.WordItem> demoWords = DemoDataProvider.getWords(8);

        for (DemoDataProvider.WordItem item : demoWords) {

            WordHuntWord word = new WordHuntWord();

            word.setWord(item.word);

            word.setDefinition(item.definition);

            word.setHint(item.hint);

            word.setGradeLevel(item.gradeLevel);

            words.add(word);

        }

        android.util.Log.d("WordHunt", "Demo mode: Loaded " + words.size() + " words");

    }



    private void startGame() {

        generateGrid();

        displayWordList();

        startTimer();

        updateProgress();

    }



    private void generateGrid() {

        grid = new char[gridSize][gridSize];

        gridCells = new TextView[gridSize][gridSize];



        // Track which cells are used by words (not random)

        boolean[][] usedCells = new boolean[gridSize][gridSize];



        // Place words in grid FIRST (before filling with random)

        for (WordHuntWord word : words) {

            placeWordInGrid(word, usedCells);

        }



        // Fill remaining cells with random letters

        Random random = new Random();

        for (int i = 0; i < gridSize; i++) {

            for (int j = 0; j < gridSize; j++) {

                if (!usedCells[i][j]) {

                    grid[i][j] = (char) ('A' + random.nextInt(26));

                }

            }

        }



        // Create grid UI

        letterGrid.removeAllViews();

        letterGrid.setColumnCount(gridSize);

        letterGrid.setRowCount(gridSize);



        int cellSize = getResources().getDisplayMetrics().widthPixels / (gridSize + 2);



        for (int row = 0; row < gridSize; row++) {

            for (int col = 0; col < gridSize; col++) {

                TextView cell = createGridCell(grid[row][col], row, col, cellSize);

                gridCells[row][col] = cell;



                GridLayout.LayoutParams params = new GridLayout.LayoutParams();

                params.width = cellSize;

                params.height = cellSize;

                params.setMargins(2, 2, 2, 2);

                params.rowSpec = GridLayout.spec(row);

                params.columnSpec = GridLayout.spec(col);



                letterGrid.addView(cell, params);

            }

        }



        setupGridTouchListener();

    }



    private void placeWordInGrid(WordHuntWord word, boolean[][] usedCells) {

        String wordStr = word.getWord().toUpperCase();

        Random random = new Random();

        int maxAttempts = 200;

        int attempts = 0;



        while (attempts < maxAttempts) {

            boolean horizontal = random.nextBoolean();

            int row, col;



            if (horizontal) {

                if (gridSize - wordStr.length() + 1 <= 0) {

                    attempts++;

                    continue;

                }

                row = random.nextInt(gridSize);

                col = random.nextInt(gridSize - wordStr.length() + 1);



                if (canPlaceWord(wordStr, row, col, 0, 1, usedCells)) {

                    for (int i = 0; i < wordStr.length(); i++) {

                        grid[row][col + i] = wordStr.charAt(i);

                        usedCells[row][col + i] = true;

                    }

                    word.setPosition(row, col, row, col + wordStr.length() - 1, true);

                    return;

                }

            } else {

                if (gridSize - wordStr.length() + 1 <= 0) {

                    attempts++;

                    continue;

                }

                row = random.nextInt(gridSize - wordStr.length() + 1);

                col = random.nextInt(gridSize);



                if (canPlaceWord(wordStr, row, col, 1, 0, usedCells)) {

                    for (int i = 0; i < wordStr.length(); i++) {

                        grid[row + i][col] = wordStr.charAt(i);

                        usedCells[row + i][col] = true;

                    }

                    word.setPosition(row, col, row + wordStr.length() - 1, col, false);

                    return;

                }

            }

            attempts++;

        }



        // Force placement horizontally if can't find spot

        for (int row = 0; row < gridSize; row++) {

            for (int col = 0; col <= gridSize - wordStr.length(); col++) {

                boolean canPlace = true;

                for (int i = 0; i < wordStr.length(); i++) {

                    if (usedCells[row][col + i]) {

                        canPlace = false;

                        break;

                    }

                }

                if (canPlace) {

                    for (int i = 0; i < wordStr.length(); i++) {

                        grid[row][col + i] = wordStr.charAt(i);

                        usedCells[row][col + i] = true;

                    }

                    word.setPosition(row, col, row, col + wordStr.length() - 1, true);

                    return;

                }

            }

        }



        // If still can't place, log error (word won't be findable)

        android.util.Log.e("WordHunt", "Could not place word: " + wordStr);

    }



    private boolean canPlaceWord(String word, int startRow, int startCol, int rowDir, int colDir, boolean[][] usedCells) {

        for (int i = 0; i < word.length(); i++) {

            int r = startRow + i * rowDir;

            int c = startCol + i * colDir;



            if (r >= gridSize || c >= gridSize || r < 0 || c < 0) return false;



            // Check if cell is already used by another word

            if (usedCells[r][c]) {

                // Allow if the existing letter matches what we need

                if (grid[r][c] != word.charAt(i)) {

                    return false;

                }

            }

        }

        return true;

    }



    private TextView createGridCell(char letter, int row, int col, int size) {

        TextView cell = new TextView(this);

        cell.setText(String.valueOf(letter));

        cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        cell.setTypeface(null, Typeface.BOLD);

        cell.setGravity(Gravity.CENTER);

        cell.setTextColor(ContextCompat.getColor(this, R.color.text_primary));

        cell.setBackgroundResource(R.drawable.grid_cell_background);

        cell.setTag(R.id.tag_row, row);

        cell.setTag(R.id.tag_col, col);

        return cell;

    }



    private void setupGridTouchListener() {

        letterGrid.setOnTouchListener((v, event) -> {

            int action = event.getAction();

            int[] gridLocation = new int[2];

            letterGrid.getLocationOnScreen(gridLocation);



            float x = event.getRawX() - gridLocation[0];

            float y = event.getRawY() - gridLocation[1];



            int cellSize = letterGrid.getWidth() / gridSize;

            int col = (int) (x / cellSize);

            int row = (int) (y / cellSize);



            if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {

                return true;

            }



            switch (action) {

                case MotionEvent.ACTION_DOWN:

                    startSelection(row, col);

                    return true;



                case MotionEvent.ACTION_MOVE:

                    updateSelection(row, col);

                    return true;



                case MotionEvent.ACTION_UP:

                    endSelection();

                    return true;

            }

            return false;

        });

    }



    private void startSelection(int row, int col) {

        isSelecting = true;

        startRow = row;

        startCol = col;

        endRow = row;

        endCol = col;

        selectedCells.clear();

        selectedCells.add(new int[]{row, col});

        highlightSelection();

    }



    private void updateSelection(int row, int col) {

        if (!isSelecting) return;



        // Only allow horizontal or vertical selection

        if (row != startRow && col != startCol) {

            // Determine dominant direction

            if (Math.abs(row - startRow) > Math.abs(col - startCol)) {

                col = startCol;

            } else {

                row = startRow;

            }

        }



        endRow = row;

        endCol = col;



        selectedCells.clear();

        if (startRow == endRow) {

            // Horizontal

            int minCol = Math.min(startCol, endCol);

            int maxCol = Math.max(startCol, endCol);

            for (int c = minCol; c <= maxCol; c++) {

                selectedCells.add(new int[]{startRow, c});

            }

        } else {

            // Vertical

            int minRow = Math.min(startRow, endRow);

            int maxRow = Math.max(startRow, endRow);

            for (int r = minRow; r <= maxRow; r++) {

                selectedCells.add(new int[]{r, startCol});

            }

        }



        highlightSelection();

    }



    private void endSelection() {

        if (!isSelecting) return;

        isSelecting = false;



        String selectedWord = getSelectedWord();

        checkWord(selectedWord);



        clearSelection();

    }



    private String getSelectedWord() {

        StringBuilder sb = new StringBuilder();

        for (int[] cell : selectedCells) {

            sb.append(grid[cell[0]][cell[1]]);

        }

        return sb.toString();

    }



    private void highlightSelection() {

        // Clear previous selection highlights (but keep found words)

        for (int r = 0; r < gridSize; r++) {

            for (int c = 0; c < gridSize; c++) {

                String key = r + "," + c;

                if (!highlightedCells.contains(key)) {

                    gridCells[r][c].setBackgroundResource(R.drawable.grid_cell_background);

                }

            }

        }



        // Highlight current selection

        for (int[] cell : selectedCells) {

            String key = cell[0] + "," + cell[1];

            if (!highlightedCells.contains(key)) {

                gridCells[cell[0]][cell[1]].setBackgroundResource(R.drawable.grid_cell_selected);

            }

        }

    }



    private void clearSelection() {

        for (int[] cell : selectedCells) {

            String key = cell[0] + "," + cell[1];

            if (!highlightedCells.contains(key)) {

                gridCells[cell[0]][cell[1]].setBackgroundResource(R.drawable.grid_cell_background);

            }

        }

        selectedCells.clear();

    }



    private void checkWord(String selectedWord) {

        for (WordHuntWord word : words) {

            if (word.isFound()) continue;



            String wordStr = word.getWord().toUpperCase();

            String reverseWord = new StringBuilder(wordStr).reverse().toString();



            if (selectedWord.equals(wordStr) || selectedWord.equals(reverseWord)) {

                foundWord(word);

                return;

            }

        }



        // Wrong selection

        playSound(SOUND_WRONG);

    }



    private void foundWord(WordHuntWord word) {

        word.setFound(true);

        wordsFound++;



        // Calculate points

        int points = POINTS_PER_WORD;

        if (timeRemaining > TOTAL_TIME * 0.5) {

            points += TIME_BONUS; // Bonus for finding quickly

        }

        score += points;



        // Highlight found cells permanently

        for (int[] cell : selectedCells) {

            String key = cell[0] + "," + cell[1];

            highlightedCells.add(key);

            gridCells[cell[0]][cell[1]].setBackgroundResource(R.drawable.grid_cell_found);

            gridCells[cell[0]][cell[1]].setTextColor(Color.WHITE);

        }



        // Update word list (strike through)

        updateWordListItem(word);



        // Show definition

        showDefinition(word);



        // Update UI

        updateScore();

        updateProgress();



        // Play sound

        playSound(SOUND_CORRECT);



        CustomToast.showSuccess(this, "Found: " + word.getWord() + " +" + points + " XP");



        // Check if all words found

        if (wordsFound >= words.size()) {

            handler.postDelayed(this::endGame, 1500);

        }

    }



    private void showDefinition(WordHuntWord word) {

        cardDefinition.setVisibility(View.VISIBLE);

        tvFoundWord.setText(word.getWord().toUpperCase());

        tvDefinition.setText(word.getDefinition());



        if (word.getExampleSentence() != null && !word.getExampleSentence().isEmpty()) {

            tvExample.setText("Example: " + word.getExampleSentence());

            tvExample.setVisibility(View.VISIBLE);

        } else {

            tvExample.setVisibility(View.GONE);

        }



        // Auto-hide after 3 seconds

        handler.postDelayed(() -> {

            cardDefinition.setVisibility(View.GONE);

        }, 3000);

    }



    private void displayWordList() {

        wordListContainer.removeAllViews();



        for (WordHuntWord word : words) {

            TextView wordView = new TextView(this);

            wordView.setText(word.getWord().toUpperCase());

            wordView.setTextSize(14);

            wordView.setTextColor(Color.WHITE);

            wordView.setPadding(16, 8, 16, 8);

            wordView.setTag(word.getWord().toUpperCase());



            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(

                    ViewGroup.LayoutParams.WRAP_CONTENT,

                    ViewGroup.LayoutParams.WRAP_CONTENT

            );

            params.setMargins(4, 4, 4, 4);

            wordView.setLayoutParams(params);



            wordListContainer.addView(wordView);

        }

    }



    private void updateWordListItem(WordHuntWord word) {

        for (int i = 0; i < wordListContainer.getChildCount(); i++) {

            View child = wordListContainer.getChildAt(i);

            if (child instanceof TextView && child.getTag() != null) {

                if (child.getTag().equals(word.getWord().toUpperCase())) {

                    TextView tv = (TextView) child;

                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    tv.setAlpha(0.5f);

                    break;

                }

            }

        }

    }



    private void useHint() {

        // Find an unfound word and highlight its first letter

        for (WordHuntWord word : words) {

            if (!word.isFound() && word.getStartRow() >= 0) {

                int row = word.getStartRow();

                int col = word.getStartCol();



                // Verify position is valid

                if (row >= gridSize || col >= gridSize) {

                    android.util.Log.e("WordHunt", "Invalid hint position for word: " + word.getWord());

                    continue;

                }



                // Pulse animation on first letter using ValueAnimator

                TextView cell = gridCells[row][col];

                int colorFrom = ContextCompat.getColor(this, R.color.gray_light);

                int colorTo = ContextCompat.getColor(this, R.color.color_sunglow);



                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

                colorAnimation.setDuration(400);

                colorAnimation.setRepeatCount(5);

                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);

                colorAnimation.addUpdateListener(animator -> {

                    cell.setBackgroundColor((int) animator.getAnimatedValue());

                });

                colorAnimation.addListener(new android.animation.AnimatorListenerAdapter() {

                    @Override

                    public void onAnimationEnd(android.animation.Animator animation) {

                        // Restore the original background drawable after animation

                        String key = row + "," + col;

                        if (highlightedCells.contains(key)) {

                            cell.setBackgroundResource(R.drawable.grid_cell_found);

                        } else {

                            cell.setBackgroundResource(R.drawable.grid_cell_background);

                        }

                    }

                });

                colorAnimation.start();



                hintsUsed++;

                score = Math.max(0, score - HINT_PENALTY);

                updateScore();



                // Log for debugging

                android.util.Log.d("WordHunt", "Hint for word: " + word.getWord() + " at [" + row + "," + col + "] = " + grid[row][col]);



                CustomToast.showInfo(this, "Look for: " + word.getWord().charAt(0) + "... at highlighted cell");

                return;

            }

        }



        CustomToast.showInfo(this, "No more hints available!");

    }



    private void shuffleGrid() {

        // Regenerate random letters (keeping placed words)

        Random random = new Random();



        for (int i = 0; i < gridSize; i++) {

            for (int j = 0; j < gridSize; j++) {

                boolean isWordCell = false;



                // Check if this cell is part of a word

                for (WordHuntWord word : words) {

                    if (word.getStartRow() >= 0) {

                        if (word.isHorizontal()) {

                            if (i == word.getStartRow() && j >= word.getStartCol() && j <= word.getEndCol()) {

                                isWordCell = true;

                                break;

                            }

                        } else {

                            if (j == word.getStartCol() && i >= word.getStartRow() && i <= word.getEndRow()) {

                                isWordCell = true;

                                break;

                            }

                        }

                    }

                }



                // Only change random filler letters, not word letters

                if (!isWordCell) {

                    grid[i][j] = (char) ('A' + random.nextInt(26));

                    gridCells[i][j].setText(String.valueOf(grid[i][j]));

                }

            }

        }



        CustomToast.showInfo(this, "Grid shuffled!");

    }



    private void startTimer() {

        timerProgress.setMax((int) (TOTAL_TIME / 1000));

        timerProgress.setProgress((int) (TOTAL_TIME / 1000));



        gameTimer = new CountDownTimer(TOTAL_TIME, 1000) {

            @Override

            public void onTick(long millisUntilFinished) {

                timeRemaining = millisUntilFinished;

                int seconds = (int) (millisUntilFinished / 1000);

                int minutes = seconds / 60;

                seconds = seconds % 60;

                tvTimer.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

                timerProgress.setProgress(seconds + (minutes * 60));



                if (millisUntilFinished < 30000) {

                    tvTimer.setTextColor(ContextCompat.getColor(WordHuntActivity.this, R.color.color_error));

                }

            }



            @Override

            public void onFinish() {

                tvTimer.setText("0:00");

                endGame();

            }

        }.start();

    }



    private void updateProgress() {

        int progress = (int) (((float) wordsFound / words.size()) * 100);

        progressBar.setProgress(progress);

        tvProgress.setText(String.format(Locale.getDefault(), "%d/%d words found", wordsFound, words.size()));

    }



    private void updateScore() {

        tvScore.setText(String.valueOf(score));

    }



    private void endGame() {

        if (gameTimer != null) {

            gameTimer.cancel();

        }



        // Bonus for finding all words

        if (wordsFound >= words.size()) {

            score += BONUS_ALL_WORDS;

        }



        long totalTime = System.currentTimeMillis() - startTime;

        float accuracy = words.size() > 0 ? ((float) wordsFound / words.size()) * 100 : 0;



        saveGameResults(accuracy, totalTime);

        showResultDialog(accuracy, totalTime);

    }



    private void saveGameResults(float accuracy, long totalTime) {

        int studentId = session.getStudentId();

        if (studentId <= 0) {

            android.util.Log.e("WordHunt", "Cannot save - invalid studentId: " + studentId);

            return;

        }



        int timeInSeconds = (int) (totalTime / 1000);



        // DEMO MODE: Save locally instead of API

        if (AppConfig.DEMO_MODE) {

            int effectiveLessonId = lessonId != null ? lessonId : 1;

            DemoDataProvider.saveGameCompleted(this, effectiveLessonId, "WordHunt", score, accuracy, timeInSeconds);

            session.updateTotalXP(DemoDataProvider.getTotalXP(this));

            android.util.Log.d("WordHunt", "Demo mode: Saved game locally - XP: " + score);

            return;

        }



        SaveGameResultRequest request = new SaveGameResultRequest.Builder(studentId, "WordHunt", score)

                .sessionId(sessionId)

                .lessonId(lessonId)

                .accuracyPercentage(accuracy)

                .timeCompleted(timeInSeconds)

                .xpEarned(score)

                .streakAchieved(wordsFound)

                .build();



        android.util.Log.d("WordHunt", "Saving game result - studentId: " + studentId +

                ", lessonId: " + lessonId + ", sessionId: " + sessionId +

                ", score: " + score + ", accuracy: " + accuracy);



        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.saveGameResult(request).enqueue(new Callback<SaveGameResultResponse>() {

            @Override

            public void onResponse(Call<SaveGameResultResponse> call, Response<SaveGameResultResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    android.util.Log.d("WordHunt", "Save response - success: " + response.body().isSuccess() +

                            ", message: " + response.body().getMessage() +

                            ", gameResultId: " + response.body().getGameResultId());



                    if (response.body().isSuccess()) {

                        SaveGameResultResponse.StudentStats stats = response.body().getStudent();

                        if (stats != null) {

                            android.util.Log.d("WordHunt", "Updated stats - TotalXP: " + stats.getTotalXP() +

                                    ", CurrentStreak: " + stats.getCurrentStreak());

                            session.updateTotalXP(stats.getTotalXP());

                        }

                    }

                } else {

                    String errorMsg = "Unknown error";

                    try {

                        if (response.errorBody() != null) {

                            errorMsg = response.errorBody().string();

                        }

                    } catch (Exception e) {

                        errorMsg = "HTTP " + response.code();

                    }

                    android.util.Log.e("WordHunt", "Save failed - HTTP " + response.code() + ": " + errorMsg);

                }

            }



            @Override

            public void onFailure(Call<SaveGameResultResponse> call, Throwable t) {

                android.util.Log.e("WordHunt", "Failed to save game result: " + t.getMessage(), t);

            }

        });

    }



    private void showResultDialog(float accuracy, long totalTime) {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);



        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);

        builder.setCancelable(false);



        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        }



        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);

        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);

        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);

        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);

        TextView tvResultTime = dialogView.findViewById(R.id.tvResultTime);

        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);

        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);



        String title;

        if (wordsFound >= words.size()) {

            title = "Word Master!";

        } else if (accuracy >= 70) {

            title = "Great Job!";

        } else if (accuracy >= 50) {

            title = "Good Effort!";

        } else {

            title = "Keep Practicing!";

        }

        tvResultTitle.setText(title);



        tvResultScore.setText(String.valueOf(score));

        tvResultAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", accuracy));

        tvResultStreak.setText(wordsFound + "/" + words.size());



        int minutes = (int) (totalTime / 60000);

        int seconds = (int) ((totalTime % 60000) / 1000);

        tvResultTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));



        tvResultXP.setText("+" + score + " XP");



        btnFinish.setOnClickListener(v -> {

            dialog.dismiss();

            finishWithResult(accuracy);

        });



        dialog.show();

    }



    private void finishWithResult(float accuracy) {

        Intent resultIntent = new Intent();

        resultIntent.putExtra("xp_earned", score);

        resultIntent.putExtra("accuracy", (int) accuracy);

        resultIntent.putExtra("correct_count", wordsFound);

        resultIntent.putExtra("total_count", words.size());

        setResult(RESULT_OK, resultIntent);

        finish();

    }



    private void showExitConfirmation() {

        new AlertDialog.Builder(this)

                .setTitle("Exit Game?")

                .setMessage("Your progress will be lost. Are you sure you want to exit?")

                .setPositiveButton("Exit", (dialog, which) -> {

                    if (gameTimer != null) gameTimer.cancel();

                    setResult(RESULT_CANCELED);

                    finish();

                })

                .setNegativeButton("Continue", null)

                .show();

    }



    private static final int SOUND_CORRECT = 1;

    private static final int SOUND_WRONG = 2;



    private void playSound(int soundType) {

        try {

            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

            switch (soundType) {

                case SOUND_CORRECT:

                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150);

                    break;

                case SOUND_WRONG:

                    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 100);

                    break;

            }

            handler.postDelayed(toneGenerator::release, 200);

        } catch (Exception e) {

            // Ignore sound errors

        }

    }



    @SuppressWarnings("deprecation")

    @Override

    public void onBackPressed() {

        showExitConfirmation();

    }



    @Override

    protected void onDestroy() {

        super.onDestroy();

        if (gameTimer != null) {

            gameTimer.cancel();

        }

        handler.removeCallbacksAndMessages(null);

    }

}