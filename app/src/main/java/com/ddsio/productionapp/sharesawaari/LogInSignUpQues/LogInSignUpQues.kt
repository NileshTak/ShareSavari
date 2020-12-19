package com.ddsio.productionapp.sharesawaari.LogInSignUpQues

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesawaari.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class LogInSignUpQues : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_sign_up_ques)

        Handler().postDelayed({
            var int = Intent(this,
                QuesBottomSheet::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this@LogInSignUpQues,
                    R.anim.slide_up, R.anim.slide_down
                ).toBundle()
            startActivity(int,bundle)
        }, 1000)




    }
}