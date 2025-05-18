// File: ProfileActivity.kt
package com.example.geekshop.View

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.geekshop.R
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.repository.ProfileRepository
import com.example.geekshop.data.utils.SharedPreferencesHelper
import com.example.geekshop.viewmodel.ProfileViewModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var sharedPrefs: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация SharedPreferences
        sharedPrefs = SharedPreferencesHelper(this)

        // Инициализация репозитория и ViewModel
        val repository = ProfileRepository(SQLite.getInstance(applicationContext))
        val viewModelFactory = ProfileViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]

        // Подписка на бонусы пользователя
        observeUserBonus()

        // Загрузка данных пользователя
        loadUserData()

        // Настройка навигации
        setupTabBar()

        // Настройка кнопки выхода
        setupLogoutButton()
    }

    /**
     * Подписываемся на изменения количества бонусов
     */
    private fun observeUserBonus() {
        viewModel.userBonus.observe(this) { bonus ->
            updateUserInfo(bonus)
        }
    }

    /**
     * Загрузка данных пользователя
     */
    private fun loadUserData() {
        val userId = sharedPrefs.userId
        if (userId != -1) {
            viewModel.loadUserBonus(userId)
        }
    }

    /**
     * Обновление информации о пользователе на экране
     * @param bonus Текущее количество бонусов
     */
    private fun updateUserInfo(bonus: Int) {
        val userName = sharedPrefs.userName ?: "Гость"
        val userLogin = sharedPrefs.userLogin ?: ""

        // Сохраняем бонус в SharedPreferences
        sharedPrefs.userBonus = bonus

        // Форматирование ID пользователя (№ 0000 0000 0000)
        val formattedId = "№ " + sharedPrefs.userId.toString()
            .padStart(12, '0')
            .chunked(4)
            .joinToString(" ")

        findViewById<TextView>(R.id.text_name).text = userName
        findViewById<TextView>(R.id.text_login).text = userLogin
        findViewById<TextView>(R.id.text_bonus).text = bonus.toString()
        findViewById<TextView>(R.id.text_number_cart).text = formattedId
    }

    /**
     * Настройка нижней панели навигации
     */
    private fun setupTabBar() {
        findViewById<ImageView>(R.id.btn_main).setOnClickListener {
            navigateTo(MainActivity::class.java)
        }

        findViewById<ImageView>(R.id.btn_cart).setOnClickListener {
            navigateTo(CartActivity::class.java)
        }
    }

    /**
     * Настройка кнопки выхода из аккаунта
     */
    private fun setupLogoutButton() {
        findViewById<Button>(R.id.button_exit).setOnClickListener {
            // Очищаем данные пользователя
            sharedPrefs.apply {
                userId = -1
                userName = null
                userLogin = null
                userBonus = 0
            }

            // Переходим на экран авторизации
            navigateTo(AuthActivity::class.java)
            finish()
        }
    }

    /**
     * Универсальный метод для навигации
     * @param destination Класс активности назначения
     */
    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
    }
}