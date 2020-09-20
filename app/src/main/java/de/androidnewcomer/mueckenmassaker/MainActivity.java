package de.androidnewcomer.mueckenmassaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Animation fadeInAnimation;
    private Animation wiggleAnimation;
    private Button startButton;
    private Handler handler = new Handler();
    private Runnable wiggleRunnable = new WiggleButton();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //fade in animation
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        //wiggle animation for the button
        wiggleAnimation = AnimationUtils.loadAnimation(this, R.anim.wiggle);
        //start button for the game
        startButton = (Button) findViewById(R.id.startButton);
        //set onClickListener for the button
        startButton.setOnClickListener(this);
    }

    //onClcikListener for the Button to start the game
    @Override
    public void onClick(View view) {
        //starts the game activity
        startActivity(new Intent(this, GameActivity.class));
    }

    //allows the slow fade in animation when you start the app
    @Override
    protected void onResume() {
        super.onResume();
        //get the root view element (background of the main activity)
        View view = findViewById(R.id.root);
        //start the fide in animation when the user accesses the activity
        view.startAnimation(fadeInAnimation);
        //wait 2000ms and then play the wiggle animation
        handler.postDelayed(wiggleRunnable, 2000);
    }

    private class WiggleButton implements Runnable {
        @Override
        public void run() {
            //start the wiggle button animation
            startButton.startAnimation(wiggleAnimation);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //remove the callback when the user is on a different activity
        handler.removeCallbacks(wiggleRunnable);
    }
}