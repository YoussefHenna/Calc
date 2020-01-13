package com.gooofystudios.calc

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import kotlinx.android.synthetic.main.activity_app_info.*

class AppInfoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_info)

        val anim = ValueAnimator.ofFloat(0.0f,1.0f)
        anim.addUpdateListener {
            infoTitle.alpha = it.animatedValue as Float
        }
        anim.duration = 100
        infoTitle.postDelayed({
            anim.start()

        },500)


        backButton.setOnClickListener {
            onBackPressed()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}