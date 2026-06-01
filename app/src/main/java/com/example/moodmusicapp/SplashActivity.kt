package com.example.moodmusicapp

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)

        // Start subtle fade animation for each dot with a staggered delay to create a left-to-right effect
        startFadeAnimation(dot1, 0)
        startFadeAnimation(dot2, 250)
        startFadeAnimation(dot3, 500)

        // Navigate to MainActivity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

    private fun startFadeAnimation(view: View, delay: Long) {
        val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.3f, 1.0f)
        animator.duration = 800
        animator.repeatMode = ValueAnimator.REVERSE
        animator.repeatCount = ValueAnimator.INFINITE
        animator.startDelay = delay
        animator.start()
    }
}