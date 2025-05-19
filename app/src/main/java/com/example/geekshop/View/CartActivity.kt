package com.example.geekshop.View

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.geekshop.R
import com.example.geekshop.data.db.SQLite
import com.example.geekshop.data.model.Products
import com.example.geekshop.repository.CartRepository
import com.example.geekshop.viewmodel.CartViewModel
import com.example.geekshop.viewmodel.CartViewModelFactory

class CartActivity : AppCompatActivity() {

    private lateinit var viewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        // Инициализация репозитория и ViewModel
        val repository = CartRepository(SQLite.getInstance(this))
        val viewModelFactory = CartViewModelFactory(repository, applicationContext)
        viewModel = ViewModelProvider(this, viewModelFactory)[CartViewModel::class.java]

        // Настройка edge-to-edge отображения
        setupEdgeToEdge()

        // Наблюдение за LiveData
        observeCartItems()
        observeTotalSum()
        observeIsBonusApplied()
        observeToastMessages()

        // Настройка UI элементов
        setupBonusSwitch()
        setupPurchaseButton()
        setupTabBar()

        // Загрузка товаров и дней аренды
        viewModel.loadCartItems()
        setupRentalDays()
    }

    // Наблюдение за сообщениями Toast
    private fun observeToastMessages() {
        viewModel.toastMessage.observe(this) { message ->
            message?.let { showToast(it) }
        }
    }

    // Настройка edge-to-edge отображения
    @SuppressLint("MissingInflatedId")
    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Наблюдение за товарами в корзине
    private fun observeCartItems() {
        viewModel.cartItems.observe(this) { items ->
            loadCartItems(items)
        }
    }

    // Наблюдение за общей суммой заказа
    private fun observeTotalSum() {
        viewModel.totalSum.observe(this) { sum ->
            findViewById<TextView>(R.id.resSum).text = "$sum ₽"
        }
    }

    // Наблюдение за применением бонусов
    private fun observeIsBonusApplied() {
        viewModel.isBonusApplied.observe(this) { applied ->
            if (!applied) {
                viewModel.resetBonuses()
            }
        }
    }

    // Настройка переключателя бонусов
    private fun setupBonusSwitch() {
        findViewById<Switch>(R.id.switch_bonus).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.applyBonuses()
            } else {
                viewModel.resetBonuses()
            }
        }
    }

    // Настройка кнопки покупки
    private fun setupPurchaseButton() {
        findViewById<Button>(R.id.button_pay).setOnClickListener {
            if (viewModel.cartItems.value.isNullOrEmpty()) {
                showToast("Корзина пуста")
            } else {
                // Сохраняем итоговую сумму (может быть 0 после применения бонусов)
                viewModel.savePurchaseAmount()
                startActivity(Intent(this, PayPal::class.java))
            }
        }
    }

    // Настройка нижней панели навигации
    private fun setupTabBar() {
        findViewById<ImageView>(R.id.btn_main).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<ImageView>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    // Загрузка товаров в корзине
    private fun loadCartItems(cartItems: List<Pair<Products, Int>>) {
        val productsContainer = findViewById<LinearLayout>(R.id.products_container)
        productsContainer.removeAllViews()
        if (cartItems.isEmpty()) {
            showEmptyCartMessage(productsContainer)
        } else {
            cartItems.forEach { (product, quantity) ->
                addProductToView(productsContainer, product, quantity)
            }
        }
    }

    // Добавление товара в представление
    private fun addProductToView(container: LinearLayout, product: Products, quantity: Int) {
        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.style_product_in_cart, container, false)
        itemView.findViewById<ImageView>(R.id.ivProductImage).setImageResource(product.imageId)
        itemView.findViewById<TextView>(R.id.tvProductName).text = product.Name
        itemView.findViewById<TextView>(R.id.tvProductPrice).text = "${product.Cost} x$quantity"
        itemView.findViewById<Button>(R.id.btnRemoveFromCart).setOnClickListener {
            viewModel.removeFromCart(product.id)
        }
        container.addView(itemView)
    }

    // Отображение сообщения о пустой корзине
    private fun showEmptyCartMessage(container: LinearLayout) {
        TextView(this).apply {
            text = "Корзина пуста"
            textSize = 18f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 50, 0, 0) }
        }.also { container.addView(it) }
    }

    // Настройка выбора дней аренды
    private fun setupRentalDays() {
        val tvQuantity = findViewById<TextView>(R.id.tvQuantity)
        var quantity = 1
        findViewById<Button>(R.id.btnIncrease).setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
            viewModel.updateTotalSum(quantity)
        }
        findViewById<Button>(R.id.btnDecrease).setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
                viewModel.updateTotalSum(quantity)
            }
        }
    }

    // Показ сообщения Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}