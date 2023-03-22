package com.yashkasera.streamchat.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.yashkasera.streamchat.MainActivity
import com.yashkasera.streamchat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun updateUiWithUser() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun showLoginFailed(errorString: String) {
        Snackbar.make(binding.root, errorString, Snackbar.LENGTH_SHORT).show()
    }
}