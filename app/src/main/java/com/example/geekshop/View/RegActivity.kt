package com.example.geekshop.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.geekshop.repository.UserRepository
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.viewmodel.UserViewModel
import com.example.geekshop.viewmodel.UserViewModelFactory

class RegActivity : AppCompatActivity() {

    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reg)

        // Initialize ViewModel
        val userRepository = UserRepository(SQLite.getInstance(applicationContext))
        val viewModelFactory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, viewModelFactory)[UserViewModel::class.java]

        // Setup edge-to-edge
        setupEdgeToEdge()

        // Setup buttons
        setupRegisterButton()
        setupAuthRedirectButton()

        // Observe LiveData
        observeRegistrationResult()
        observeIsLoginExists()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRegisterButton() {
        findViewById<AppCompatButton>(R.id.btn_reg).setOnClickListener {
            val name = findViewById<EditText>(R.id.editText_name).text.toString().trim()
            val login = findViewById<EditText>(R.id.editText_login).text.toString().trim()
            val password = findViewById<EditText>(R.id.editText_password).text.toString().trim()

            if (!validateInput(name, login, password)) return@setOnClickListener

            // Проверяем существование логина
            userViewModel.checkLoginExists(login)
        }
    }

    private fun observeIsLoginExists() {
        userViewModel.isLoginExists.observe(this) { exists ->
            if (exists) {
                showToast("Логин уже занят!")
            } else {
                // Если логин свободен, продолжаем регистрацию
                val name = findViewById<EditText>(R.id.editText_name).text.toString().trim()
                val login = findViewById<EditText>(R.id.editText_login).text.toString().trim()
                val password = findViewById<EditText>(R.id.editText_password).text.toString().trim()

                userViewModel.registerUser(name, login, password)
            }
        }
    }

    private fun validateInput(name: String, login: String, password: String): Boolean {
        if (name.isEmpty() || login.isEmpty() || password.isEmpty()) {
            showToast("Заполните все поля!")
            return false
        }
        return true
    }

    private fun setupAuthRedirectButton() {
        findViewById<TextView>(R.id.btn_go_auth).setOnClickListener {
            navigateTo(AuthActivity::class.java)
        }
    }

    private fun observeRegistrationResult() {
        userViewModel.registrationResult.observe(this) { userId ->
            if (userId != -1L) {
                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                showToast("Ошибка при создании пользователя")
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}