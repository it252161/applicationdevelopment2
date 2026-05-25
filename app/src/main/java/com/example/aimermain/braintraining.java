package com.example.aimermain;

import android.os.Bundle;
import android.widget.Button;
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

// クラス名を MainActivity にします
public class braintraining extends AppCompatActivity {

    private TextView tvQuestion, tvScore;
    private Button[] btnAnswers = new Button[4];
    private List<QuestionData> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;

    class QuestionData {
        String question;
        String[] choices;
        String answer;
        QuestionData(String question, String[] choices, String answer) {
            this.question = question;
            this.choices = choices;
            this.answer = answer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ここで使うレイアウトが braintraining.xml であることを確認
        setContentView(R.layout.braintraining);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        btnAnswers[0] = findViewById(R.id.btnAnswer1);
        btnAnswers[1] = findViewById(R.id.btnAnswer2);
        btnAnswers[2] = findViewById(R.id.btnAnswer3);
        btnAnswers[3] = findViewById(R.id.btnAnswer4);

        // ボタンが見つかるかチェック
        for (int i = 0; i < 4; i++) {
            if (btnAnswers[i] == null) {
                Toast.makeText(this, "エラー: ボタン" + (i+1) + "がXMLで見つかりません", Toast.LENGTH_LONG).show();
                return;
            }
        }

        loadQuestionsFromJSON();
        Collections.shuffle(questionList);
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
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                JSONArray choiceArray = obj.getJSONArray("choices");
                String[] choices = new String[4];
                for (int j = 0; j < 4; j++) choices[j] = choiceArray.getString(j);
                questionList.add(new QuestionData(obj.getString("question"), choices, obj.getString("answer")));
            }
        } catch (Exception e) {
            Toast.makeText(this, "JSON読み込み失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            QuestionData currentQ = questionList.get(currentQuestionIndex);
            tvQuestion.setText(currentQ.question);
            for (int i = 0; i < 4; i++) {
                btnAnswers[i].setText(currentQ.choices[i]);
                final String selected = currentQ.choices[i];
                btnAnswers[i].setOnClickListener(v -> checkAnswer(selected, currentQ.answer));
            }
        } else {
            tvQuestion.setText("全問終了！");
        }
    }

    private void checkAnswer(String selected, String correct) {
        if (selected.equals(correct)) {
            score++;
            tvScore.setText("正解数: " + score);
        }
        currentQuestionIndex++;
        displayQuestion();
    }
}