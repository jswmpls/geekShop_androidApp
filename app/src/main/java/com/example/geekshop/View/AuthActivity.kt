package com.example.geekshop.View

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.geekshop.R
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Users
import com.example.geekshop.repository.UserRepository
import com.example.geekshop.viewmodel.UserViewModel
import com.example.geekshop.viewmodel.UserViewModelFactory

class AuthActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        // Инициализация ViewModel
        val userRepository = UserRepository(SQLite.getInstance(applicationContext))
        val viewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        // Настройка edge-to-edge
        setupEdgeToEdge()

        // Инициализация элементов UI
        val userLogin: EditText = findViewById(R.id.editText_login_auth)
        val userPassword: EditText = findViewById(R.id.editText_password_auth)
        val btnAuth: AppCompatButton = findViewById(R.id.btn_auth)
        val btnGoReg: TextView = findViewById(R.id.btn_go_reg)

        // Обработка клика по кнопке авторизации
        btnAuth.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()
            if (login.isEmpty() || password.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }
            userViewModel.authenticateUser(login, password)
            userViewModel.authenticationResult.observe(this) { user ->
                if (user != null) {
                    saveUserData(user)
                    navigateToMain()
                } else {
                    showToast("Неверный логин или пароль")
                }
            }
        }

        // Обработка клика по кнопке регистрации
        btnGoReg.setOnClickListener {
            navigateToRegistration()
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun saveUserData(user: Users) {
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
            putString("user_name", user.name)
            putString("user_login", user.login)
            putInt("user_bonus", user.bonus)
            putInt("current_user_id", user.id)
            apply()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun navigateToRegistration() {
        startActivity(Intent(this, RegActivity::class.java))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}