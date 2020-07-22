package com.nil.productionapp.sharesavari.LogInSignUpQues

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import com.nil.productionapp.sharesavari.MainActivity
import com.nil.productionapp.sharesavari.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_ques_bottom_sheet.*

class QuesBottomSheet : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ques_bottom_sheet)


        window.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        tvLogIn.setOnClickListener {
            var int = Intent(this,
                MainActivity::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this@QuesBottomSheet,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            startActivity(int,bundle)
            finishAffinity()
        }
    }
}