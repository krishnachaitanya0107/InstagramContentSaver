package com.example.instagramcontentsaver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.text.HtmlCompat
import com.example.instagramcontentsaver.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    lateinit var binding:ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivitySplashScreenBinding.inflate(layoutInflater)

        supportActionBar?.hide()

        binding.splashText.text = HtmlCompat.fromHtml(
            "Download <font color='#6200EE'>Instagram</font> photos , videos ,<br/> <center>Reels and IgTv Videos</center>",
            0
        )

        Handler().postDelayed(
            {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
                finish()
            }
            ,2000)

        setContentView(binding.root)
    }


}