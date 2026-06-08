package com.example.aimermain;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class GenreSelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_select); // 次に作成するXML

        setupGenreButton(R.id.btnMath, "計算");
        setupGenreButton(R.id.btnKanji, "漢字");
        setupGenreButton(R.id.btnLogic, "論理");
        setupGenreButton(R.id.btnAll, "すべて");
    }

    private void setupGenreButton(int id, String genreName) {
        findViewById(id).setOnClickListener(v -> {
            Intent intent = new Intent(this, braintraining.class);
            intent.putExtra("SELECTED_GENRE", genreName);
            startActivity(intent);
        });
    }
}