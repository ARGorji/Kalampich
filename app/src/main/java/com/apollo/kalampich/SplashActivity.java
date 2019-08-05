package com.apollo.kalampich;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    MediaPlayer mpSplashSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mpSplashSound = MediaPlayer.create(this, R.raw.startup);
        mpSplashSound.start();

        ImageView imgKaaf = (ImageView)findViewById(R.id.imgKaaf);
        ImageView imgLaam = (ImageView)findViewById(R.id.imgLaam);
        ImageView imgMim = (ImageView)findViewById(R.id.imgMim);
        ImageView imgP = (ImageView)findViewById(R.id.imgP);
        ImageView imgYa = (ImageView)findViewById(R.id.imgYa);
        ImageView imgCh = (ImageView)findViewById(R.id.imgCh);

        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shake2);
        imgKaaf.startAnimation(animFadein);
        imgLaam.startAnimation(animFadein);
        imgMim.startAnimation(animFadein);
        imgP.startAnimation(animFadein);
        imgYa.startAnimation(animFadein);
        imgCh.startAnimation(animFadein);

        int secondsDelayed = 6;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, secondsDelayed * 1000);
    }

    public void GoToMainScreen(View v0) {
        try {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();

        } catch (Exception err) {

        }
    }

}
