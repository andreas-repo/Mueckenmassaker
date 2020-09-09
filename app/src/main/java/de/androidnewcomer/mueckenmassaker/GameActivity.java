package de.androidnewcomer.mueckenmassaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameActivity extends AppCompatActivity {

    private boolean gameRunning = false;
    private int round;
    private int remainingMosquitos:
    private int caughtMosquitos;
    private int time;
    private int points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void startGame() {
        gameRunning = true;
        round = 0;
        points = 0;
        startRound();
    }

    private void startRound() {
        round++;
        remainingMosquitos = round * 10;
        caughtMosquitos = 0;
        time = 60;
        refreshDisplay();
    }


}