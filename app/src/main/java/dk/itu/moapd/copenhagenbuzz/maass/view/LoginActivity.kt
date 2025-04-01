package dk.itu.moapd.copenhagenbuzz.maass.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dk.itu.moapd.copenhagenbuzz.maass.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button click listeners
        binding.btnLogin.setOnClickListener {
            // For now, simulate login (replace with actual login logic later)
            startMainActivity(true) // Logged in state
        }

        binding.btnGuest.setOnClickListener {
            startMainActivity(false) // Guest state
        }
    }

    private fun startMainActivity(isLoggedIn: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isLoggedIn", isLoggedIn)
        startActivity(intent)
        finish() // Close LoginActivity after switching
    }
}