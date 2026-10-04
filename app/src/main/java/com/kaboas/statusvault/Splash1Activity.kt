package com.kaboas.statusvault

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Splash1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash1)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Splash2Activity::class.java))
            finish()
        }, 3000)
    }
}
