package com.example.aimermain;

import android.annotation.SuppressLint;
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
    private List<QuizRecord> history = new ArrayList<>();

    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;
    private final long TIME_LIMIT = 10000; // 10秒

    // 履歴保存用クラス
    class QuizRecord {
        String question; String userAns; String correctAns; boolean isCorrect;
        QuizRecord(String q, String u, String c, boolean res) {
            this.question = q; this.userAns = u; this.correctAns = c; this.isCorrect = res;
        }
    }

    // 問題データクラス（JSONの新しい構造に対応）
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

        // ジャンル絞り込み（将来的にIntent等で選択したジャンルを受け取る想定）
        filterByGenre("計算");

        Collections.shuffle(questionList);
        if (questionList.size() > 10) {
            questionList = new ArrayList<>(questionList.subList(0, 10));
        }

        progressBar.setMax(questionList.size());
        displayQuestion();
    }

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
            Toast.makeText(this, "問題の読み込みに失敗しました", Toast.LENGTH_SHORT).show();
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
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("残り: " + String.format("%.1f", millisUntilFinished / 1000.0) + "秒");
                if (millisUntilFinished < 3000) tvTimer.setTextColor(Color.RED);
                else tvTimer.setTextColor(Color.BLACK);
            }
            public void onFinish() {
                tvTimer.setText("時間切れ！");
                checkAnswer("TIMEOUT", questionList.get(currentQuestionIndex));
            }
        }.start();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            progressBar.setProgress(currentQuestionIndex + 1);
            QuestionData currentQ = questionList.get(currentQuestionIndex);
            tvQuestion.setText(currentQ.question);
            tvQuestion.setBackgroundColor(Color.TRANSPARENT); // 背景リセット

            List<String> shuffledChoices = new ArrayList<>();
            for (String s : currentQ.choices) shuffledChoices.add(s);
            Collections.shuffle(shuffledChoices);

            for (int i = 0; i < 4; i++) {
                String choiceText = shuffledChoices.get(i);
                btnAnswers[i].setText(choiceText);
                btnAnswers[i].setEnabled(true);
                btnAnswers[i].setOnClickListener(v -> {
                    if (countDownTimer != null) countDownTimer.cancel();
                    checkAnswer(choiceText, currentQ);
                });
            }
            startTimer();
        } else {
            showFinalResult();
        }
    }

    private void checkAnswer(String selected, QuestionData currentQ) {
        for (Button btn : btnAnswers) btn.setEnabled(false); // 二重押し防止

        boolean isCorrect = selected.equals(currentQ.answer);
        if (isCorrect) {
            score++;
            tvQuestion.setBackgroundColor(Color.parseColor("#C8E6C9")); // 薄い緑
        } else {
            tvQuestion.setBackgroundColor(Color.parseColor("#FFCDD2")); // 薄い赤
        }

        history.add(new QuizRecord(currentQ.question, selected, currentQ.answer, isCorrect));
        tvScore.setText("Score: " + score);

        // 1秒後に次の問題へ
        tvQuestion.postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1000);
    }

    private void showFinalResult() {
        if (countDownTimer != null) countDownTimer.cancel();
        saveHighScore();

        int highScore = getSharedPreferences("BrainGame", MODE_PRIVATE).getInt("hi_score", 0);

        String resultText = "【終了！】\n" +
                "正解数: " + score + " / " + questionList.size() + "\n" +
                "自己ベスト: " + highScore;

        tvQuestion.setText(resultText);
        tvTimer.setText("お疲れ様でした！");
        for (Button btn : btnAnswers) btn.setEnabled(false);
    }

    private void saveHighScore() {
        SharedPreferences pref = getSharedPreferences("BrainGame", MODE_PRIVATE);
        int lastHighScore = pref.getInt("hi_score", 0);
        if (score > lastHighScore) {
            pref.edit().putInt("hi_score", score).apply();
            Toast.makeText(this, "ハイスコア更新！", Toast.LENGTH_LONG).show();
        }
    }
}