package com.yashkasera.streamchat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yashkasera.streamchat.data.model.LoggedInUser
import com.yashkasera.streamchat.databinding.ActivitySplashBinding
import com.yashkasera.streamchat.ui.login.LoginActivity
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val loggedInUser = LoggedInUser.getOrNull()
        if (loggedInUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            lifecycleScope.launch {
                AppObjectController.initUser(loggedInUser)
                startActivity(
                    Intent(
                        this@SplashActivity,
                        MainActivity::class.java
                    )
                )
                finish()
            }
        }
    }
}