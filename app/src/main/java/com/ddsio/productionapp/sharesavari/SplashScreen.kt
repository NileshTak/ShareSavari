package com.ddsio.productionapp.sharesavari

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesavari.Intro.IntroActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val img =
            findViewById<View>(R.id.img) as ImageView
        val aniFade = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fade_in
        )
        img.startAnimation(aniFade)
        Handler().postDelayed({
            val bundle = ActivityOptionsCompat.makeCustomAnimation(
                this@SplashScreen,
                android.R.anim.fade_in, android.R.anim.fade_out
            ).toBundle()
            startActivity(Intent(this@SplashScreen, IntroActivity::class.java), bundle)
            finish()
        }, 2000)
    }
}