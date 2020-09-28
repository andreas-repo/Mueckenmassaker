package de.androidnewcomer.mueckenmassaker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, Runnable {

    private static final long MAX_AGE_MS = 2000;
    private static final int DELAY_MILLIS = 1000;
    private static final int MULTIPLICATION_FACTOR = 10;
    private static final int TIME_SLICE = 600;
    private static final String INSECT = "insect";
    private static final int movementSpeedMultiplier = 2;

    private boolean gameRunning;
    private int round;
    private int mosquitos;
    private int caughtMosquitos;
    private int time;
    private int points;
    private float scale;

    private ViewGroup gameArea;
    private MediaPlayer mediaPlayer;
    private Random random = new Random();
    private Handler handler = new Handler();
    private AudioManager audioManager;
    Animation animationCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //get the pixel density of the mobile phone
        scale = getResources().getDisplayMetrics().density;
        //initialize/get the game area
        gameArea = (ViewGroup) findViewById(R.id.gameAreaFrameLayout);
        //initialize mediaplayer object
        mediaPlayer = MediaPlayer.create(this, R.raw.summen);

        //TODO find a way to resolve the volume problem => change root audio volume

        //execute the startGame() method
        startGame();

    }

    //startGame() method
    private void startGame() {
        //set the boolean gameRunning variable to true
        gameRunning = true;
        //set the round variable to "0"
        round = 0;
        //set the points variable to "0"
        points = 0;
        //execute the startRound() method
        startRound();
    }

    private void startRound() {
        //increment the round varialbe by "1"
        round++;
        //set the amount of mosquitos to the value of  "round * MULTIPLICATION_FACTOR"
        mosquitos = round * MULTIPLICATION_FACTOR;
        //set the amount of caught mosquitos to "0"
        caughtMosquitos = 0;
        //set the time of the round
        time = TIME_SLICE;
        //execute refreshDisplay() method
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

    //method to refresh the contents of the display
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

    //method to count down the time and spawn mosquitos
    private void countdownTime() {
        //decrement the time by "1"
        time--;
        //checks if the modulo results into a "0"
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
        //execute removeMosquitos() method
        removeMosquitos();

        //call mosquitoMovement() method
        mosquitoMovement();

        //execute refreshDisplay() method
        refreshDisplay();
        //checks if the game is over...
        if (!checkGameover()) {
            //..checks if the round is over
            if(!checkRoundEnding()) {
                handler.postDelayed(this, 1000);
            }
        }
    }

    //method to check if it's game over
    private boolean checkGameover() {
        //if the time is "0" or if enough mosquitos have been caught...
        if (time == 0 && caughtMosquitos < mosquitos) {
            //...then the gameover() method is been executed...
            gameover();
            //...and returns true
            return true;
        }
        //...else if returns false
        return false;
    }

    //method to check if the round is ending
    private boolean checkRoundEnding() {
        //if the amount of caught mosquitos is bigger than the remaining mosquitos then...
        if (caughtMosquitos >= mosquitos) {
            //...it starts a new round...
            startRound();
            //...and returns true
            return true;
        }
        //...else it returns false
        return false;
    }

    //method to spawn a new mosquito
    private void spawnOneMosquito() {
        //creates a image view for the mosquito
        ImageView spawnedMosquito = new ImageView(this);
        //add a onClickListener for the mosquito
        spawnedMosquito.setOnClickListener(this);
        //gets the height of the game area
        int height = gameArea.getHeight();
        //gets the width of the game area
        int width = gameArea.getWidth();

        int mosquitoHeight = Math.round(scale * 50);
        int mosquitoWidth = Math.round(scale * 42);

        //set the velocity of the mosquitos vectors X & Y randomly
        int vectorX;
        int vectorY;
        do {
            vectorX = random.nextInt(3)-1;
            vectorY = random.nextInt(3)-1;
        } while (vectorX == 0 || vectorY == 0);

        //add vectorX and vectorY as a tag
        spawnedMosquito.setTag(R.id.vectorX, vectorX);
        spawnedMosquito.setTag(R.id.vectorY, vectorY);
        if (random.nextFloat() < 0.05) {
            //set the scorpion image
            spawnedMosquito.setImageResource(R.drawable.insect);
            //set the tag R.id.insect with the value 'INSECT'
            spawnedMosquito.setTag(R.id.insect, INSECT);
        } else {

            //execute setMosquitoMovement() method which set the correct image for the mosquito movement
            setMosquitoImage(spawnedMosquito, vectorX, vectorY);
        }

        //now the vectors have to be multiplicated with the scale of the screen
        vectorX = (int) Math.round(scale * vectorX);
        vectorY = (int) Math.round(scale * vectorY);



        //creates a random value to place the mosquito inside the game area
        int left = random.nextInt(width - mosquitoWidth);
        int top = random.nextInt(height - mosquitoHeight);





        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mosquitoWidth, mosquitoHeight);
        //place the mosquito via the randomly generated values
        params.leftMargin = left;
        params.topMargin = top;
        params.gravity = Gravity.TOP + Gravity.LEFT;

        //Fade_In animation
        animationCreation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animationCreation.setAnimationListener(new MosquitoCreationAnimationListener());
        spawnedMosquito.startAnimation(animationCreation);

        //add the mosquito image view to the game area
        gameArea.addView(spawnedMosquito, params);

        //set a tag for the newly generated mosquito which contains a birth date for the mosquito to  later determine if the mosquito must be removed
        spawnedMosquito.setTag(R.id.birthdate, new Date());


        //Animation animationCreation = AnimationUtils.loadAnimation(this, R.anim.mosquito_creation);
        spawnedMosquito.startAnimation(animationCreation);

        //reset media player to second '0'
        mediaPlayer.seekTo(0);
        //start to play the 'summen' audio file
        mediaPlayer.start();
    }

    //method for the movement of the mosquito's
    private void mosquitoMovement() {
        int number = 0;
        //while to number is lower than the amount of child's on the game area (all mosquito's')
        while (number < gameArea.getChildCount()) {
            //get the layout view of the mosquito with the index of the "number" variable....
            ImageView mosquito = (ImageView) gameArea.getChildAt(number);
            //get vector of the X axis via the tag....
            int vectorX = (Integer) mosquito.getTag(R.id.vectorX);
            //get the vector of the Y axis via the tag...
            int vectorY = (Integer) mosquito.getTag(R.id.vectorY);
            //get the parameters of the mosquito as a variable
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mosquito.getLayoutParams();
            //add the X vector to the left margin...
            params.leftMargin += vectorX * (round * movementSpeedMultiplier);
            //add the Y vector to the top margin...
            params.topMargin += vectorY * (round * movementSpeedMultiplier);
            //add the changed parameters to the mosquito
            mosquito.setLayoutParams(params);
            number++;
        }
    }

    private void setMosquitoImage(ImageView mosquito, int vectorX, int vectorY) {
        if (vectorX == -1 && vectorY == -1) {
            mosquito.setImageResource(R.drawable.muecke_nw);
        }
        if (vectorX == -1 && vectorY == 0) {
            mosquito.setImageResource(R.drawable.muecke_w);
        }
        if (vectorX == -1 && vectorY == +1) {
            mosquito.setImageResource(R.drawable.muecke_sw);
        }
        if (vectorX == 0 && vectorY == -1) {
            mosquito.setImageResource(R.drawable.muecke_n);
        }
        if (vectorX == 0 && vectorY == +1) {
            mosquito.setImageResource(R.drawable.muecke_s);
        }
        if (vectorX == +1 && vectorY == -1) {
            mosquito.setImageResource(R.drawable.muecke_no);
        }
        if (vectorX         == +1 && vectorY == 0) {
            mosquito.setImageResource(R.drawable.muecke_o);
        }
        if (vectorX == +1 && vectorY == +1) {
            mosquito.setImageResource(R.drawable.muecke_so);
        }
    }

    //method to remove mosquitos
    private void removeMosquitos() {
        int number = 0;
        //while the number variable is smaller than the amount of children...
        while ( number < gameArea.getChildCount()) {
            //...get the child from the game area...
            ImageView mosquito = (ImageView) gameArea.getChildAt(number);
            //read and save the birthdate of the child as a variable...
            Date birthDate = (Date) mosquito.getTag(R.id.birthdate);
            //calculate of the age...
            long age = (new Date()).getTime() - birthDate.getTime();
            //...and if´the age is bigger than the maximum age...
            if ( age > MAX_AGE_MS) {
                //...then remove the mosquito from the game area
                gameArea.removeView(mosquito); //incrementation is not needed because the next mosquito slides to this index number cuz of the removeView method
            } else {
                //...else it increments the number by '1'
                number++;
            }
        }
    }

    //method for game over
    public void gameover() {
        //create a new dialog for the game over message
        Dialog gameoverDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        //set the dialog's content view to the gameover.xml
        gameoverDialog.setContentView(R.layout.gameover);
        //set the dialog to visible/show
        gameoverDialog.show();
        //set the gameRunning variable to false;
        gameRunning = false;
    }

    //the onclick listener for the catching a mosquito via touchscreen
    @Override
    public void onClick(View mosquito) {
        //if the tag of the view equals 'INSECT'...
        if (mosquito.getTag(R.id.insect) == INSECT) {
            //...then the player looses 1000 points
            points -= 1000;
        //else...
        } else {
            //increment the caughtMosquito variable
            caughtMosquitos++;
            //add 100 points to the points variable
            points = points + 100;
        }
        //pause media player audio because mosquito has been clicked on
        mediaPlayer.pause();

        //create the hit animation...
        Animation animationHit = AnimationUtils.loadAnimation(this, R.anim.hit);
        //start the animation
        mosquito.startAnimation(animationHit);
        //...and set the MosquitoAnimationListener and then...
        animationHit.setAnimationListener(new MosquitoAnimationListener(mosquito));
        //...you remove the onClick Listener from the mosquito to avoid that the player gets points if he clicks on the spot where the hit mosquito was.
        mosquito.setOnClickListener(null);

        //refresh the display
        refreshDisplay();

    }

    //thread run
    @Override
    public void run() {
        //execute the countdownTime() method
        countdownTime();
    }

    //release media player when activity is closed
    @Override
    protected void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(this);
    }

    //this class is for the mosquito fade out animation
    private class MosquitoAnimationListener implements Animation.AnimationListener {
        //reference needed to play the animation on the correct mosquito object
        private View mosquito;

        //this class creator makes sure a view object (the mosquito) is added as the reference
        public MosquitoAnimationListener(View mosquito) {
            this.mosquito = mosquito;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            //the handler + runnable are needed to avoid problems which can result in the unwanted crash of the application
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //cancel the spawn 'fade in' animation
                    animationCreation.cancel();
                    //remove the mosquito view from game area
                    gameArea.removeView(mosquito);
                }
            });
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private class MosquitoCreationAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


}