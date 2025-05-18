package com.example.geekshop.View

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.geekshop.R
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.repository.PayPalRepository
import com.example.geekshop.viewmodel.PayPalViewModel

class PayPal : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var viewModel: PayPalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pay_pal)

        setupEdgeToEdge()

        // Инициализация SharedPreferences
        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)

        // Инициализация Repository и ViewModel
        val repository = PayPalRepository(SQLite.getInstance(this))
        viewModel = ViewModelProvider(this, PayPalViewModel.provideFactory(repository))[PayPalViewModel::class.java]

        // Наблюдение за результатом обработки платежа
        observePaymentResult()

        // Настройка кнопки оплаты
        setupPayButton()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupPayButton() {
        findViewById<Button>(R.id.button_buy).setOnClickListener {
            val userId = sharedPref.getInt("current_user_id", -1)
            val purchaseAmount = sharedPref.getInt("purchase_amount", 0)
            val usedBonus = sharedPref.getInt("used_bonus", 0)

            if (userId == -1) {
                Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Измененная проверка - разрешаем покупку за 0 рублей
            if (purchaseAmount < 0) {
                Toast.makeText(this, "Ошибка: сумма покупки некорректна", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.processPayment(userId, purchaseAmount, usedBonus)
        }
    }

    private fun observePaymentResult() {
        viewModel.bonusUpdateResult.observe(this) { result ->
            val (_, bonusToAdd) = result
            Toast.makeText(
                this,
                "Покупка совершена!\nНачислено $bonusToAdd бонусов",
                Toast.LENGTH_LONG
            ).show()
        }

        viewModel.paymentProcessed.observe(this) { success ->
            if (success) {
                navigateToMain()
            } else {
                Toast.makeText(this, "Ошибка при обработке платежа", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        // Очистка временных данных
        val editor = sharedPref.edit()
        editor.remove("purchase_amount")
        editor.remove("used_bonus")
        editor.apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}