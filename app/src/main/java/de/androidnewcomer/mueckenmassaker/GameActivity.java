package de.androidnewcomer.mueckenmassaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    private static final long MAX_AGE_MS = 2000;
    public static final int DELAY_MILLIS = 1000;
    public static final int MULTIPLICATION_FACTOR = 10;
    public static final int TIME_SLICE = 600;

    private boolean gameRunning;
    private int round;
    private int mosquitos;
    private int caughtMosquitos;
    private int time;
    private int points;

    private float scale;
    private Random random = new Random();
    private ViewGroup gameArea;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        scale = getResources().getDisplayMetrics().density;
        gameArea = (ViewGroup) findViewById(R.id.gameAreaFrameLayout);
        startGame();
    }

    private void startGame() {
        gameRunning = true;
        round = 0;
        points = 0;
        startRound();

    }

    private void startRound() {
        round++;
        mosquitos = round * MULTIPLICATION_FACTOR;
        caughtMosquitos = 0;
        time = TIME_SLICE;
        refreshDisplay();
        handler.postDelayed(this, DELAY_MILLIS);
        //get the id from  the resource called "background" + number from the drawable folder
        int number = random.nextInt(4);
        int id = getResources().getIdentifier("background"+ number, "drawable", this.getPackageName());
        if (id > 0) {
            //Use the id to change the game background
            ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.background);
            constraintLayout.setBackground(getDrawable(id));
        }
    }

    private void refreshDisplay() {
        TextView textViewPoints = (TextView) findViewById(R.id.pointsText);
        textViewPoints.setText(Integer.toString(points));
        TextView textViewRound = (TextView) findViewById(R.id.roundText);
        textViewRound.setText(Integer.toString(round));
        TextView textViewHits = (TextView) findViewById(R.id.hits_text);
        textViewHits.setText(Integer.toString(caughtMosquitos));
        TextView textViewTime = (TextView) findViewById(R.id.time_text);
        textViewTime.setText(Integer.toString(time/(1000/DELAY_MILLIS)));

        FrameLayout frameLayoutHits = (FrameLayout) findViewById(R.id.bar_hits);
        FrameLayout frameLayoutTime = (FrameLayout) findViewById(R.id.bar_time);
        ViewGroup.LayoutParams layoutParamsHits = frameLayoutHits.getLayoutParams();
        ViewGroup.LayoutParams layoutParamsTime = frameLayoutTime.getLayoutParams();
        layoutParamsHits.width = Math.round(scale * 300 * Math.min(caughtMosquitos, mosquitos) / mosquitos);
        layoutParamsTime.width = Math.round(scale * time * 300 / 60);
    }

    private void countdownTime() {
        time--;
        if ( time % (1000 / DELAY_MILLIS) == 0) {
            float randomNumber = random.nextFloat();
            double probability = mosquitos * 1.5;
            if (probability > 1) {
                spawnOneMosquito();
                if (randomNumber < probability - 1) {
                    spawnOneMosquito();
                }
            } else {
                if (randomNumber < probability) {
                    spawnOneMosquito();
                }
            }
        }
        removeMosquitos();
        refreshDisplay();
        if (!checkGameover()) {
            if(!checkRoundEnding()) {
                handler.postDelayed(this, 1000);
            }
        }
    }

    private boolean checkGameover() {
        if (time == 0 && caughtMosquitos < mosquitos) {
            gameover();
            return true;
        }
        return false;
    }

    private boolean checkRoundEnding() {
        if (caughtMosquitos >= mosquitos) {
            startRound();
            return true;
        }
        return false;
    }

    private void spawnOneMosquito() {
        int heigth = gameArea.getHeight();
        int width = gameArea.getWidth();
        int mosquitoHeigth = Math.round(scale * 50);
        int mosquitoWidth = Math.round(scale * 42);

        int left = random.nextInt(width - mosquitoWidth);
        int top = random.nextInt(heigth - mosquitoHeigth);

        ImageView spawnedMosquito = new ImageView(this);
        spawnedMosquito.setImageResource(R.drawable.muecke);
        spawnedMosquito.setOnClickListener(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mosquitoWidth, mosquitoHeigth);
        params.leftMargin = left;
        params.topMargin = top;
        params.gravity = Gravity.TOP + Gravity.LEFT;

        gameArea.addView(spawnedMosquito, params);
        spawnedMosquito.setTag(R.id.birthdate, new Date());
    }

    private void removeMosquitos() {
        int number = 0;
        while ( number < gameArea.getChildCount()) {
            ImageView mosquito = (ImageView) gameArea.getChildAt(number);
            Date birthDate = (Date) mosquito.getTag(R.id.birthdate);
            long age = (new Date()).getTime() - birthDate.getTime();
            if ( age > MAX_AGE_MS) {
                gameArea.removeView(mosquito);
            } else {
                number++;
            }
        }
    }

    public void gameover() {
        Dialog gameoverDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        gameoverDialog.setContentView(R.layout.gameover);
        gameoverDialog.show();
        gameRunning = false;
    }

    @Override
    public void onClick(View view) {
        caughtMosquitos++;
        points = points + 100;
        refreshDisplay();
        gameArea.removeView(view);
    }

    @Override
    public void run() {
        countdownTime();
    }
}