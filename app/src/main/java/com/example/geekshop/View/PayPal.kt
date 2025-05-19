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

/**
 * Экран обработки платежа через PayPal
 */
class PayPal : AppCompatActivity() {

    // SharedPreferences для хранения данных пользователя
    private lateinit var sharedPref: SharedPreferences
    private lateinit var viewModel: PayPalViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Включаем edge-to-edge отображение
        setContentView(R.layout.activity_pay_pal)

        setupEdgeToEdge()
        initSharedPreferences()
        initViewModel()
        setupPayButton()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initSharedPreferences() {
        sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
    }

    private fun initViewModel() {
        val repository = PayPalRepository(SQLite.getInstance(this))
        viewModel = ViewModelProvider(
            this,
            PayPalViewModel.provideFactory(repository)
        )[PayPalViewModel::class.java]

        // Наблюдаем за результатами платежа
        observePaymentResult()
    }

    private fun setupPayButton() {
        findViewById<Button>(R.id.button_buy).setOnClickListener {
            val userId = sharedPref.getInt("current_user_id", -1).takeIf { it != -1 }
                ?: run {
                    Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            val purchaseAmount = sharedPref.getInt("purchase_amount", 0)

            // Проверяем корректность суммы (разрешены покупки за 0 рублей)
            if (purchaseAmount < 0) {
                Toast.makeText(this, "Ошибка: сумма покупки некорректна", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usedBonus = sharedPref.getInt("used_bonus", 0)
            viewModel.processPayment(userId, purchaseAmount, usedBonus)
        }
    }

    private fun observePaymentResult() {
        viewModel.bonusUpdateResult.observe(this) { (_, bonusToAdd) ->
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
        // Очищаем временные данные о покупке
        with(sharedPref.edit()) {
            remove("purchase_amount")
            remove("used_bonus")
            apply()
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}