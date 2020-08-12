package com.ddsio.productionapp.sharesavari;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.ddsio.productionapp.sharesavari.Intro.IntroActivity;
import com.ddsio.productionapp.sharesavari.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        ImageView img = (ImageView)findViewById(R.id.img);
        Animation aniFade = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fade_in);
        img.startAnimation(aniFade);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(SplashScreen.this,
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(new Intent(SplashScreen.this, IntroActivity.class),bundle);

                finish();
            }
        },2000);
    }
}
