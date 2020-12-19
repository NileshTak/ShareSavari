package com.ddsio.productionapp.sharesawaari.LogInSignUpQues

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesawaari.MainActivity
import com.ddsio.productionapp.sharesawaari.R
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
//            EventBus.getDefault().post("LogIn");

            var int = Intent(this,
                MainActivity::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this@QuesBottomSheet,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("type","LogIn")
            startActivity(int,bundle)
        }

        btnSignUp.setOnClickListener {
//            EventBus.getDefault().post("SignUp");

            var int = Intent(this,
                MainActivity::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this@QuesBottomSheet,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("type","SignUp")
            startActivity(int,bundle)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCompat.finishAffinity(this)
    }

}