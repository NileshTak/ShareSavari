package com.ddsio.productionapp.sharesavari.SearchScreen.child

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.ddsio.productionapp.sharesavari.MainActivity
import com.ddsio.productionapp.sharesavari.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class BookedSuccess : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booked_success)

        Toast.makeText(this@BookedSuccess,"Ride Booked Successfully.", Toast.LENGTH_LONG).show()


        Handler().postDelayed({
            var int = Intent(this,
                MainActivity::class.java)
            val bundle =
                ActivityOptionsCompat.makeCustomAnimation(
                    this ,
                    R.anim.fade_in, R.anim.fade_out
                ).toBundle()
            int.putExtra("type", "")
            startActivity(int,bundle)
        }, 2000)


    }
}