package com.akhnaton.foodvisits.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.akhnaton.foodvisits.R
import com.akhnaton.foodvisits.ui.home.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach)


        val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
        animationView.setAnimation("noodles.json")

//        animationView.addAnimatorListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                super.onAnimationEnd(animation)
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//                finishAffinity()
//            }
//        })

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finishAffinity()
        }, 3400)

    }
}