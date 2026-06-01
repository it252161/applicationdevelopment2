package com.example.aimermain;

import android.annotation.SuppressLint;
import android.content.Intent; // 追加
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class braintraining extends AppCompatActivity {

    private TextView tvQuestion, tvScore, tvTimer;
    private ProgressBar progressBar;
    private Button[] btnAnswers = new Button[4];
    private List<QuestionData> questionList = new ArrayList<>();

    // 結果画面に渡すために履歴クラスを少し変更
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;
    private final long TIME_LIMIT = 10000;

    // 内部クラス（既存）
    class QuestionData {
        String question; String[] choices; String answer; String genre; String explanation;
        QuestionData(String question, String[] choices, String answer, String genre, String explanation) {
            this.question = question; this.choices = choices; this.answer = answer;
            this.genre = genre; this.explanation = explanation;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.braintraining);

        // Viewの紐付け
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);
        btnAnswers[0] = findViewById(R.id.btnAnswer1);
        btnAnswers[1] = findViewById(R.id.btnAnswer2);
        btnAnswers[2] = findViewById(R.id.btnAnswer3);
        btnAnswers[3] = findViewById(R.id.btnAnswer4);

        loadQuestionsFromJSON();

        // ★追記：ジャンル選択画面から送られてきた値を受け取る
        String selectedGenre = getIntent().getStringExtra("SELECTED_GENRE");
        if (selectedGenre == null) selectedGenre = "すべて";
        filterByGenre(selectedGenre);

        Collections.shuffle(questionList);
        if (questionList.size() > 10) {
            questionList = new ArrayList<>(questionList.subList(0, 10));
        }

        progressBar.setMax(questionList.size());
        displayQuestion();
    }

    // --- メソッド群（既存のものはそのまま、showFinalResultのみ書き換え） ---

    private void loadQuestionsFromJSON() {
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(jsonString);

            questionList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONArray choiceArray = obj.getJSONArray("choices");
                String[] choices = new String[4];
                for (int j = 0; j < 4; j++) choices[j] = choiceArray.getString(j);

                questionList.add(new QuestionData(
                        obj.getString("question"),
                        choices,
                        obj.getString("answer"),
                        obj.optString("genre", "一般"),
                        obj.optString("explanation", "")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterByGenre(String targetGenre) {
        if (targetGenre.equals("すべて")) return;
        List<QuestionData> filtered = new ArrayList<>();
        for (QuestionData q : questionList) {
            if (q.genre.equals(targetGenre)) filtered.add(q);
        }
        if (!filtered.isEmpty()) questionList = filtered;
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(TIME_LIMIT, 100) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("残り: " + String.format("%.1f", millisUntilFinished / 1000.0) + "秒");
            }
            public void onFinish() {
                checkAnswer("TIMEOUT", questionList.get(currentQuestionIndex));
            }
        }.start();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            progressBar.setProgress(currentQuestionIndex + 1);
            QuestionData currentQ = questionList.get(currentQuestionIndex);
            tvQuestion.setText(currentQ.question);
            tvQuestion.setBackgroundColor(Color.TRANSPARENT);

            List<String> shuffledChoices = new ArrayList<>();
            for (String s : currentQ.choices) shuffledChoices.add(s);
            Collections.shuffle(shuffledChoices);

            for (int i = 0; i < 4; i++) {
                String choiceText = shuffledChoices.get(i);
                btnAnswers[i].setText(choiceText);
                btnAnswers[i].setEnabled(true);
                btnAnswers[i].setOnClickListener(v -> {
                    countDownTimer.cancel();
                    checkAnswer(choiceText, currentQ);
                });
            }
            startTimer();
        } else {
            showFinalResult();
        }
    }

    private void checkAnswer(String selected, QuestionData currentQ) {
        for (Button btn : btnAnswers) btn.setEnabled(false);
        boolean isCorrect = selected.equals(currentQ.answer);
        if (isCorrect) {
            score++;
            tvQuestion.setBackgroundColor(Color.parseColor("#C8E6C9"));
        } else {
            tvQuestion.setBackgroundColor(Color.parseColor("#FFCDD2"));
        }
        tvScore.setText("Score: " + score);

        tvQuestion.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1000);
    }

    // ★書き換え：結果を表示するのではなく、結果画面へ遷移させる
    private void showFinalResult() {
        if (countDownTimer != null) countDownTimer.cancel();
        saveHighScore();

        // 結果画面 (ResultActivity) へのインテントを作成
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SCORE", score);
        intent.putExtra("TOTAL", questionList.size());

        startActivity(intent);
        finish(); // クイズ画面を終了して戻れないようにする
    }

    private void saveHighScore() {
        SharedPreferences pref = getSharedPreferences("BrainGame", MODE_PRIVATE);
        int lastHighScore = pref.getInt("hi_score", 0);
        if (score > lastHighScore) {
            pref.edit().putInt("hi_score", score).apply();
        }
    }
}