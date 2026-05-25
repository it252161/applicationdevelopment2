package com.example.aimermain;

public class Question {
    private String questionText;
    private String[] choices;
    private int answerIndex; // 0, 1, 2, 3 のいずれか

    public Question(String questionText, String[] choices, int answerIndex) {
        this.questionText = questionText;
        this.choices = choices;
        this.answerIndex = answerIndex;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getChoices() {
        return choices;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }
}